import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { RECOMMENDATION_GENRES, RecommendationGenre } from '../../recommendation.model';

@Component({
  selector: 'app-genre-select',
  imports: [MatFormFieldModule, MatSelectModule],
  templateUrl: './genre-select.component.html',
  styleUrl: './genre-select.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GenreSelectComponent {
  readonly genre = input.required<RecommendationGenre>();
  readonly genreChange = output<RecommendationGenre>();

  protected readonly genres = RECOMMENDATION_GENRES;

  protected onGenreChange(genre: RecommendationGenre): void {
    this.genreChange.emit(genre);
  }
}
