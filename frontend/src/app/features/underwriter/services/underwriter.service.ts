import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VehicleApplication } from '../../../shared/models/policy.model';

@Injectable({
    providedIn: 'root'
})
export class UnderwriterService {
    private http = inject(HttpClient);
    private readonly API_URL = 'http://localhost:8080/api/underwriter';

    getAssignedApplications(): Observable<VehicleApplication[]> {
        return this.http.get<VehicleApplication[]>(`${this.API_URL}/applications`);
    }

    approveApplication(id: number): Observable<void> {
        return this.http.post<void>(`${this.API_URL}/applications/${id}/approve`, {});
    }

    rejectApplication(id: number, reason: string): Observable<void> {
        return this.http.post<void>(`${this.API_URL}/applications/${id}/reject`, { reason });
    }
}
