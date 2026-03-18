import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CreateClaimRequest {
    policyId: number;
    claimType: 'THEFT' | 'DAMAGE' | 'THIRD_PARTY';
    description?: string;
    bankAccountNumber?: string;
    ifscCode?: string;
    accountHolderName?: string;
}

export interface ReadClaimDTO {
    claimId: number;
    claimNumber: string;
    claimType: string;
    status: string;
    approvedAmount: number;
    policyId: number;
    policyNumber: string;
    customerName: string;
    document1Path?: string;
    document2Path?: string;
    document3Path?: string;
    rejectionReason?: string;
    bankAccountNumber?: string;
    ifscCode?: string;
    accountHolderName?: string;
}

@Injectable({
    providedIn: 'root'
})
export class ClaimService {
    private readonly API_URL = 'http://localhost:8080/api/customer';
    private http = inject(HttpClient);

    submitClaim(claimData: CreateClaimRequest, doc1?: File, doc2?: File, doc3?: File): Observable<ReadClaimDTO> {
        const formData = new FormData();
        // Backend expects "claim" part to be a string (ObjectMapper.readValue)
        formData.append('claim', JSON.stringify(claimData));

        if (doc1) formData.append('doc1', doc1);
        if (doc2) formData.append('doc2', doc2);
        if (doc3) formData.append('doc3', doc3);

        return this.http.post<ReadClaimDTO>(`${this.API_URL}/submit-claim`, formData);
    }

    getMyClaims(): Observable<ReadClaimDTO[]> {
        return this.http.get<ReadClaimDTO[]>(`${this.API_URL}/my-claims`);
    }

    getAllClaims(): Observable<ReadClaimDTO[]> {
        // This would be for Admin/Claims Officer later
        return this.http.get<ReadClaimDTO[]>(`http://localhost:8080/api/claims/all`);
    }
}
