import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ApiService, Chicken, Feed } from './api.service';

type Tab = 'feed' | 'mine' | 'chickens';

@Component({ selector: 'app-root', imports: [FormsModule, DatePipe, DecimalPipe], templateUrl: './app.component.html' })
export class AppComponent implements OnInit {
  private api = inject(ApiService);
  user = this.api.user;

  tab = signal<Tab>('feed');
  chickens = signal<Chicken[]>([]);
  feed = signal<Feed[]>([]);
  mine = signal<Feed[]>([]);
  list = computed(() => (this.tab() === 'mine' ? this.mine() : this.feed()));
  error = signal('');

  // Login / Registrierung
  authMode = signal<'login' | 'register' | null>(null);
  pending: number | null = null;   // Hähnchen, das nach dem Login eingecheckt werden soll
  name = ''; password = '';

  // Check-in
  target = signal<Chicken | null>(null);
  rating = 0; comment = '';

  // Neues Hähnchen
  adding = signal(false);
  draft: Partial<Chicken> = {};

  stars = [1, 2, 3, 4, 5];

  ngOnInit() { this.reload(); }

  reload() {
    this.api.chickens().subscribe({ next: c => this.chickens.set(c), error: e => this.fail(e, 'Server nicht erreichbar.') });
    this.api.feed().subscribe({ next: f => this.feed.set(f), error: e => this.fail(e, 'Server nicht erreichbar.') });
    if (this.user()) this.api.myFeed().subscribe({ next: f => this.mine.set(f), error: e => this.fail(e, '') });
    else this.mine.set([]);
  }

  private fail(e: HttpErrorResponse, msg: string) {
    if (e.status === 401 && this.user()) { this.api.logout(); this.error.set('Sitzung abgelaufen, bitte neu anmelden.'); }
    else if (msg) this.error.set(msg);
  }

  openCheckIn(id: number) {
    if (!this.user()) { this.pending = id; this.authMode.set('login'); return; }
    this.target.set(this.chickens().find(c => c.id === id) ?? null);
    this.rating = 0; this.comment = '';
  }

  submitAuth() {
    const mode = this.authMode();
    if (!mode) return;
    this.error.set('');
    const call = mode === 'login' ? this.api.login(this.name, this.password) : this.api.register(this.name, this.password);
    call.subscribe({
      next: () => {
        this.authMode.set(null); this.password = '';
        this.reload();
        if (this.pending) { const id = this.pending; this.pending = null; setTimeout(() => this.openCheckIn(id), 300); }
      },
      error: (e: HttpErrorResponse) => this.error.set(e.status === 409 ? 'Dieser Name ist schon vergeben.' : e.status === 401 ? 'Name oder Passwort falsch.' : 'Eingabe prüfen: Name 3–30 Zeichen (Buchstaben, Zahlen, _ . -), Passwort mind. 8 Zeichen.')
    });
  }

  logout() { this.api.logout(); this.mine.set([]); if (this.tab() === 'mine') this.tab.set('feed'); }

  submitCheckIn() {
    const t = this.target();
    if (!t || !this.rating) return;
    this.api.checkIn(t.id, { rating: this.rating, comment: this.comment }).subscribe({
      next: () => { this.target.set(null); this.tab.set('feed'); this.reload(); },
      error: e => this.fail(e, 'Check-in fehlgeschlagen.')
    });
  }

  submitChicken() {
    if (!this.draft.name?.trim()) return;
    this.api.addChicken(this.draft).subscribe({
      next: id => { this.adding.set(false); this.draft = {}; this.api.chickens().subscribe(c => { this.chickens.set(c); this.openCheckIn(id); }); },
      error: e => this.fail(e, 'Hähnchen konnte nicht angelegt werden.')
    });
  }

  addClicked() { if (this.user()) this.adding.set(true); else { this.pending = null; this.authMode.set('login'); } }
}
