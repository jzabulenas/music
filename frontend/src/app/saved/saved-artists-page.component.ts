import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-saved-artists-page',
  template: '<p>Saved Artists</p>',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SavedArtistsPageComponent {}
