import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Recommendation } from './recommendation.model';

@Injectable({ providedIn: 'root' })
export class RecommendationService {
  private readonly http = inject(HttpClient);

  readonly recommendations = signal<Recommendation[]>([]);

  load(): Observable<Recommendation[]> {
    return this.http
      .get<Recommendation[]>('/api/v1/recommendations')
      .pipe(tap((recs) => this.recommendations.set(recs)));
  }

  generate(): Observable<Recommendation[]> {
    return this.http
      .post<Recommendation[]>('/api/v1/recommendations/generate', null)
      .pipe(tap((recs) => this.recommendations.set(recs)));
  }

  remove(recommendation: Recommendation): void {
    this.recommendations.update((recs) => recs.filter((r) => r.id !== recommendation.id));
  }

  // Puts a previously removed recommendation back at its original position. Defensive: a
  // no-op if the recommendation is somehow already back in the list.
  restore(recommendation: Recommendation, index: number): void {
    this.recommendations.update((recs) => {
      if (recs.some((r) => r.id === recommendation.id)) {
        return recs;
      }

      const restored = [...recs];

      restored.splice(Math.min(index, restored.length), 0, recommendation);

      return restored;
    });
  }
}
