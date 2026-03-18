export type VehicleType = 'CAR' | 'BIKE' | 'TRUCK' | 'BUS' | 'AUTO' | 'VAN';
export type FuelType = 'PETROL' | 'DIESEL' | 'ELECTRIC' | 'CNG' | 'HYBRID';
export type ApplicationStatus = 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';

export interface Vehicle {
    vehicleId: number;
    registrationNumber: string;
    make: string;
    model: string;
    year: number;
    fuelType: FuelType;
    vehicleType: VehicleType;
    customerId: number;
}

export interface VehicleApplication {
    vehicleApplicationId: number;
    registrationNumber: string;
    make: string;
    model: string;
    year: number;
    fuelType: string;
    vehicleType: string;
    status: ApplicationStatus;
    rejectionReason?: string;
    createdAt: string;
    customerName?: string;
    customerId: number;
    assignedUnderwriterId?: number;
    assignedUnderwriterName?: string;
    planId?: number;
    planName?: string;
}

export interface CreateVehicleApplicationRequest {
    registrationNumber: string;
    make: string;
    model: string;
    year: number;
    fuelType: string;
    vehicleType: string;
    planId: number;
}
