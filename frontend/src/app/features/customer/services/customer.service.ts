import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PolicyPlan {
    planId: number;
    planName: string;
    policyType: string;
    description: string;
    basePremium: number;
    maxCoverageAmount: number;
    policyDurationMonths: number;
    deductibleAmount: number;
    applicableVehicleType: string;
    active: boolean;
}

export interface QuoteRequest {
    vehicleOwnerName: string;
    registrationNumber: string;
    make: string;
    model: string;
    year: number;
    fuelType: string;
    chassisNumber: string;
    distanceDriven: number;
    exShowroomPrice: number;
    vehicleType: string;
    transmissionType: string;
    accidentsInPast: number;
}

export interface QuoteOption {
    tenureYears: number;
    calculatedPremium: number;
}

export interface QuoteResponse {
    planId: number;
    planName: string;
    policyType: string;
    description: string;
    basePremium: number;
    idv: number;
    riskLevel: string;
    options: QuoteOption[];
}

@Injectable({
    providedIn: 'root'
})
export class CustomerService {
    private readonly API_URL = 'http://localhost:8080/api/customer';

    private http = inject(HttpClient);

    // Get active plans for a specific vehicle type 
    getActivePlans(type?: string): Observable<PolicyPlan[]> {
        let url = `${this.API_URL}/policy-plans`;
        if (type) {
            url += `?type=${type}`;
        }
        return this.http.get<PolicyPlan[]>(url);
    }

    // Get quotes for a specific vehicle application profile
    getQuotes(quoteRequest: QuoteRequest): Observable<QuoteResponse[]> {
        return this.http.post<QuoteResponse[]>(`${this.API_URL}/quote`, quoteRequest);
    }

    // Submit the multipart form
    submitBuyPolicy(applicationBlob: any, rcFile: File, invoiceFile: File): Observable<string> {
        const formData = new FormData();

        // Spring Boot expects the application DTO as a JSON string named 'application'
        formData.append('application', JSON.stringify(applicationBlob));

        // Append the exact multipart files
        formData.append('rcDocument', rcFile);
        formData.append('invoiceDocument', invoiceFile);

        // Make the POST request
        return this.http.post(`${this.API_URL}/buy-policy`, formData, { responseType: 'text' });
    }

    // Get user's submitted applications
    getMyApplications(): Observable<any[]> {
        return this.http.get<any[]>(`${this.API_URL}/applications`);
    }

    // Get user's policies
    getMyPolicies(): Observable<any[]> {
        return this.http.get<any[]>(`${this.API_URL}/policies`);
    }

    // Process payment
    processPayment(paymentInfo: any): Observable<any> {
        return this.http.post(`http://localhost:8080/api/payments/process`, paymentInfo, { responseType: 'text' });
    }

    // Razorpay Integration
    createRazorpayOrder(paymentInfo: any): Observable<any> {
        return this.http.post(`http://localhost:8080/api/payments/create-order`, paymentInfo);
    }

    verifyRazorpayPayment(verificationData: any): Observable<any> {
        return this.http.post(`http://localhost:8080/api/payments/verify`, verificationData, { responseType: 'text' });
    }

    // Download documents as Blob to include Authorization headers
    downloadInvoice(policyId: number): Observable<Blob> {
        return this.http.get(`http://localhost:8080/api/payments/download/invoice/${policyId}`, { responseType: 'blob' });
    }

    downloadPolicyDocument(policyId: number): Observable<Blob> {
        return this.http.get(`http://localhost:8080/api/payments/download/policy/${policyId}`, { responseType: 'blob' });
    }
}
