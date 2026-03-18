import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { PolicyPlan, CreatePolicyPlanRequest, StaffResponse, CreateStaffRequest, VehicleApplication, UpdateApplicationStatusRequest, UnderwriterWorkload, Claim, ClaimsOfficerWorkload } from '../../../shared/models/policy.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
    private readonly API_URL = 'http://localhost:8080/api/admin';
    private http = inject(HttpClient);

    // ── Staff ─────────────────────────────────────────────
    createStaff(dto: CreateStaffRequest): Observable<any> {
        return this.http.post(`${this.API_URL}/staff`, dto)
            .pipe(catchError((e) => throwError(() => e)));
    }

    getAllStaff(): Observable<StaffResponse[]> {
        return this.http.get<StaffResponse[]>(`${this.API_URL}/staff`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    getStaffById(id: number): Observable<StaffResponse> {
        return this.http.get<StaffResponse>(`${this.API_URL}/staff/${id}`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    deactivateStaff(id: number): Observable<StaffResponse> {
        return this.http.delete<StaffResponse>(`${this.API_URL}/staff/${id}`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    activateStaff(id: number): Observable<StaffResponse> {
        return this.http.put<StaffResponse>(`${this.API_URL}/staff/${id}/activate`, {})
            .pipe(catchError((e) => throwError(() => e)));
    }

    // ── Policy Plans ──────────────────────────────────────
    addPolicyPlan(dto: CreatePolicyPlanRequest): Observable<PolicyPlan> {
        return this.http.post<PolicyPlan>(`${this.API_URL}/policy-plans`, dto)
            .pipe(catchError((e) => throwError(() => e)));
    }

    getAllPolicyPlans(): Observable<PolicyPlan[]> {
        return this.http.get<PolicyPlan[]>(`${this.API_URL}/policy-plans`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    getPolicyPlanById(id: number): Observable<PolicyPlan> {
        return this.http.get<PolicyPlan>(`${this.API_URL}/policy-plans/${id}`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    deactivatePolicyPlan(id: number): Observable<PolicyPlan> {
        return this.http.delete<PolicyPlan>(`${this.API_URL}/policy-plans/${id}`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    activatePolicyPlan(id: number): Observable<PolicyPlan> {
        return this.http.put<PolicyPlan>(`${this.API_URL}/policy-plans/${id}/activate`, {})
            .pipe(catchError((e) => throwError(() => e)));
    }

    // ── Vehicle Applications ────────────────────────────────
    getAllApplications(): Observable<VehicleApplication[]> {
        return this.http.get<VehicleApplication[]>(`${this.API_URL}/applications`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    updateApplicationStatus(id: number, request: UpdateApplicationStatusRequest): Observable<VehicleApplication> {
        return this.http.put<VehicleApplication>(`${this.API_URL}/applications/${id}/status`, request)
            .pipe(catchError((e) => throwError(() => e)));
    }

    getUnderwritersByWorkload(): Observable<UnderwriterWorkload[]> {
        return this.http.get<UnderwriterWorkload[]>(`${this.API_URL}/underwriters/workload`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    assignApplication(appId: number, underwriterId: number): Observable<VehicleApplication> {
        return this.http.put<VehicleApplication>(`${this.API_URL}/applications/${appId}/assign/${underwriterId}`, {})
            .pipe(catchError((e) => throwError(() => e)));
    }

    // ── Claims ─────────────────────────────────────────────
    getAllClaims(): Observable<Claim[]> {
        return this.http.get<Claim[]>(`${this.API_URL}/claims`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    getClaimsOfficerWorkload(): Observable<ClaimsOfficerWorkload[]> {
        return this.http.get<ClaimsOfficerWorkload[]>(`${this.API_URL}/claims-officers/workload`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    assignClaim(claimId: number, officerId: number): Observable<Claim> {
        return this.http.put<Claim>(`${this.API_URL}/claims/${claimId}/assign/${officerId}`, {})
            .pipe(catchError((e) => throwError(() => e)));
    }

    getPendingPayments(): Observable<Claim[]> {
        return this.http.get<Claim[]>(`${this.API_URL}/claims/pending-payments`)
            .pipe(catchError((e) => throwError(() => e)));
    }

    processClaimPayment(claimId: number, payload: { transactionReference: string }): Observable<any> {
        return this.http.post<any>(`${this.API_URL}/claims/${claimId}/pay`, payload)
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

    getAllPolicies(): Observable<any[]> {
        return this.http.get<any[]>(`${this.API_URL}/policies`)
            .pipe(catchError((e) => throwError(() => e)));
    }
}
