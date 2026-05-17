import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { ArtistChipComponent } from './item/artist-chip.component';
import { LikedArtist } from '../../liked-artist.model';

@Component({
  selector: 'app-artist-list',
  imports: [ArtistChipComponent],
  templateUrl: './artist-list.component.html',
  styleUrl: './artist-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArtistListComponent {
  artists = input.required<LikedArtist[]>();
  deleted = output<number>();
}
