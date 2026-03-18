import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { Claim, Payment, ClaimsPayment } from '../../../shared/models/policy.model';

@Injectable({ providedIn: 'root' })
export class AdminPaymentsService {
    private readonly API_URL = 'http://localhost:8080/api/admin';
    private http = inject(HttpClient);

    getPendingPayments(): Observable<Claim[]> {
        return this.http.get<Claim[]>(`${this.API_URL}/claims/pending-payments`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    processClaimPayment(claimId: number): Observable<any> {
        return this.http.post<any>(`${this.API_URL}/claims/${claimId}/pay`, {})
            .pipe(catchError((e) => throwError(() => e)));
    }

    getReceivedPayments(): Observable<Payment[]> {
        return this.http.get<Payment[]>(`${this.API_URL}/received-payments`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    getClaimPayouts(): Observable<ClaimsPayment[]> {
        return this.http.get<ClaimsPayment[]>(`${this.API_URL}/claims/payouts`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    exportReceivedPayments(): Observable<Blob> {
        return this.http.get(`${this.API_URL}/export/received-payments`, { responseType: 'blob' })
            .pipe(catchError((e) => throwError(() => e)));
    }

    exportPayoutHistory(): Observable<Blob> {
        return this.http.get(`${this.API_URL}/export/payout-history`, { responseType: 'blob' })
            .pipe(catchError((e) => throwError(() => e)));
    }
}
