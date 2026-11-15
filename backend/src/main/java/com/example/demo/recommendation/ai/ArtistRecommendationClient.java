package com.example.demo.recommendation.ai;

import com.example.demo.recommendation.RecommendationGenre;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class ArtistRecommendationClient {

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

    if (response == null) {
      throw new IllegalStateException("AI client returned null response");
    }

    List<RecommendedArtist> artists = response.artists();

    if (artists == null) {
      throw new IllegalStateException("AI client returned null artists");
    }

    return artists;
  }
}
