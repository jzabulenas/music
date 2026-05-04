import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-artists-page',
  template: '<p>Artists</p>',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArtistsPageComponent {}
