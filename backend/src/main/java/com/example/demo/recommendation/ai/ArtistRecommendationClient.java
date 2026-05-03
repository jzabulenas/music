package com.example.demo.recommendation.ai;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class ArtistRecommendationClient {

  private final ChatClient chatClient;

  ArtistRecommendationClient(ChatClient.Builder builder) {
    this.chatClient = builder.build();
  }

  public List<RecommendedArtist> recommend(List<String> likedArtists) {
    String artistList = String.join(", ", likedArtists);

    RecommendedArtistResponse response = this.chatClient
      .prompt()
      .user(u ->
        u
          .text(
            """
            I love these artists: {artists}.
            Suggest exactly 5 music artists I haven't listed that I'd likely enjoy.
            For each provide: name, primary genre, one-sentence reason why I'd like them.
            Don't repeat any artist from my list.
            """
          )
          .param("artists", artistList)
      )
      .call()
      .entity(RecommendedArtistResponse.class);

    return response.artists();
  }
}
