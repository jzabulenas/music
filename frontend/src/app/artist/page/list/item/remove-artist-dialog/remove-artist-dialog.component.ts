import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-remove-artist-dialog',
  imports: [MatButtonModule, MatDialogModule],
  templateUrl: './remove-artist-dialog.component.html',
  styleUrl: './remove-artist-dialog.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RemoveArtistDialogComponent {
  protected readonly artistName: string = inject(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<RemoveArtistDialogComponent>);

  protected onConfirm(): void {
    this.dialogRef.close(true);
  }

  protected onCancel(): void {
    this.dialogRef.close(false);
  }
}
