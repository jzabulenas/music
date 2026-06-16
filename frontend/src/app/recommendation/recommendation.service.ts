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
}
