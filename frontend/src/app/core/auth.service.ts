import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of, tap } from 'rxjs';
import { User } from './user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  readonly currentUser = signal<User | null>(null);

  fetchCurrentUser(): Observable<User | null> {
    return this.http.get<User>('/api/v1/me').pipe(
      tap((user) => this.currentUser.set(user)),
      catchError(() => {
        this.currentUser.set(null);

        return of(null);
      }),
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>('/logout', {}).pipe(
      tap(() => this.currentUser.set(null)),
    );
  }
}
