import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-recommendations-empty-state',
  templateUrl: './recommendations-empty-state.component.html',
  styleUrl: './recommendations-empty-state.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecommendationsEmptyStateComponent {}
