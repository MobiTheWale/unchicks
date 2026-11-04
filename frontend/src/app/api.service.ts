import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs';

export interface Chicken { id: number; name: string; vendor: string; city: string; description: string; avgRating: number; checkIns: number; }
export interface Feed { id: number; chickenId: number; chickenName: string; vendor: string; username: string; rating: number; comment: string; createdAt: string; }
interface Session { token: string; username: string; }

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const t = localStorage.getItem('unchicks.token');
  return next(t && req.url.startsWith('/api') ? req.clone({ setHeaders: { Authorization: `Bearer ${t}` } }) : req);
};

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  user = signal<string | null>(localStorage.getItem('unchicks.user'));

  private store = (s: Session) => { localStorage.setItem('unchicks.token', s.token); localStorage.setItem('unchicks.user', s.username); this.user.set(s.username); };
  logout() { localStorage.removeItem('unchicks.token'); localStorage.removeItem('unchicks.user'); this.user.set(null); }
  login = (username: string, password: string) => this.http.post<Session>('/api/auth/login', { username, password }).pipe(tap(this.store));
  register = (username: string, password: string) => this.http.post<Session>('/api/auth/register', { username, password }).pipe(tap(this.store));

  chickens = () => this.http.get<Chicken[]>('/api/chickens');
  feed = () => this.http.get<Feed[]>('/api/checkins');
  myFeed = () => this.http.get<Feed[]>('/api/checkins/mine');
  addChicken = (c: Partial<Chicken>) => this.http.post<number>('/api/chickens', c);
  checkIn = (id: number, body: { rating: number; comment: string }) => this.http.post<number>(`/api/chickens/${id}/checkins`, body);
}
