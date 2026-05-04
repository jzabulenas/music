import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-recommendations-page',
  template: '<p>Recommendations</p>',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecommendationsPageComponent {}
