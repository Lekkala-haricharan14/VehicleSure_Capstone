import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { Claim } from '../../../shared/models/policy.model';

@Injectable({
    providedIn: 'root'
})
export class ClaimsOfficerService {
    private readonly API_URL = 'http://localhost:8080/api/claims-officer';
    private http = inject(HttpClient);

    getAssignedClaims(): Observable<Claim[]> {
        return this.http.get<Claim[]>(`${this.API_URL}/claims`)
            .pipe(catchError(e => throwError(() => e)));
    }

    calculatePayment(claimId: number, payload: { billAmount?: number, exShowroomPrice?: number, yearOfManufacture?: number }): Observable<number> {
        return this.http.post<number>(`${this.API_URL}/claims/${claimId}/payout`, payload)
            .pipe(catchError(e => throwError(() => e)));
    }

    approveClaim(claimId: number, payload: { billAmount?: number, exShowroomPrice?: number, yearOfManufacture?: number }): Observable<void> {
        return this.http.post<void>(`${this.API_URL}/claims/${claimId}/approve`, payload)
            .pipe(catchError(e => throwError(() => e)));
    }

    rejectClaim(claimId: number, reason?: string): Observable<void> {
        return this.http.post<void>(`${this.API_URL}/claims/${claimId}/reject`, { reason })
            .pipe(catchError(e => throwError(() => e)));
    }
}
