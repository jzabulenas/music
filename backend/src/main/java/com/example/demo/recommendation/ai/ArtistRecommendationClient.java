package com.example.demo.recommendation.ai;

import com.example.demo.recommendation.RecommendationGenre;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class ArtistRecommendationClient {

  // The model very rarely returns a usable-but-empty response (for example `{}`): JSON mode
  // guarantees valid JSON, not a filled-in schema. Retrying costs nothing on the happy path
  // and keeps such transient misbehaviour from surfacing to the user as a 500.
  private static final int MAX_ATTEMPTS = 3;

  private static final Logger log = LoggerFactory.getLogger(
    ArtistRecommendationClient.class
  );

  private final ChatClient chatClient;

  ArtistRecommendationClient(ChatClient.Builder builder) {
    this.chatClient = builder.build();
  }

  public List<RecommendedArtist> recommend(
    List<String> likedArtists,
    List<String> rejectedArtists,
    List<String> savedArtists,
    @Nullable RecommendationGenre genre
  ) {
    String artistList = String.join(", ", likedArtists);
    String rejectedList = rejectedArtists.isEmpty()
      ? "none"
      : String.join(", ", rejectedArtists);
    String savedList = savedArtists.isEmpty()
      ? "none"
      : String.join(", ", savedArtists);

    // An empty instruction leaves the prompt unchanged: `Default` means any genre.
    String genreInstruction =
      genre == null
        ? ""
        : "Every suggested artist must be a " +
          genre.displayName() +
          " artist: report " +
          genre.displayName() +
          " as the primary genre of each one.";

    for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      RecommendedArtistResponse response = this.chatClient
        .prompt()
        .user(u ->
          u
            .text(
              """
              I love these artists: {artists}.
              Suggest exactly 8 music artists I haven't listed that I'd likely enjoy.
              For each provide: name, primary genre, one-sentence reason why I'd like them.
              {genre}
              Don't repeat any artist from my list.
              Never suggest any of these artists, as I've already rejected them: {rejected}.
              Never suggest any of these artists either, as I've already saved them for later: {saved}.
              """
            )
            .param("artists", artistList)
            .param("genre", genreInstruction)
            .param("rejected", rejectedList)
            .param("saved", savedList)
        )
        .call()
        .entity(RecommendedArtistResponse.class);

      List<RecommendedArtist> artists =
        response == null ? null : response.artists();

      // An empty list is as unusable as a null one: `generate` would wipe the user's
      // current recommendations and store nothing.
      if (artists != null && !artists.isEmpty()) {
        return artists;
      }

      log.warn(
        "AI attempt {}/{} returned no usable artists",
        attempt,
        MAX_ATTEMPTS
      );
    }

    throw new IllegalStateException(
      "AI client returned no usable artists after " + MAX_ATTEMPTS + " attempts"
    );
  }
}
