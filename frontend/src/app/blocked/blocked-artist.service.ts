import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class BlockedArtistService {
  private readonly http = inject(HttpClient);

  block(name: string): Observable<void> {
    return this.http.post<void>('/api/v1/blocked-artists', { name });
  }

  list(): Observable<string[]> {
    return this.http.get<string[]>('/api/v1/blocked-artists');
  }
}
