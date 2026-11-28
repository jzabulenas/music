import { ChangeDetectionStrategy, Component, inject, input, output } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { LikedArtist } from '../../../liked-artist.model';
import { RemoveArtistDialogComponent } from './remove-artist-dialog/remove-artist-dialog.component';

@Component({
  selector: 'app-artist-chip',
  imports: [MatChipsModule, MatDialogModule, MatIconModule],
  templateUrl: './artist-chip.component.html',
  styleUrl: './artist-chip.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArtistChipComponent {
  artist = input.required<LikedArtist>();
  deleted = output<number>();
  private readonly dialog = inject(MatDialog);

  protected onRemove(): void {
    const dialogRef = this.dialog.open(RemoveArtistDialogComponent, {
      data: this.artist().name,
      ariaLabel: `Remove ${this.artist().name}?`,
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed === true) {
        this.deleted.emit(this.artist().id);
      }
    });
  }
}
