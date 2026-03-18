export type PolicyStatus = 'ACTIVE' | 'EXPIRED' | 'CANCELLED' | 'PENDING' | 'PENDING_PAYMENT' | 'PAID';
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED';
export type ClaimType = 'THEFT' | 'DAMAGE' | 'THIRD_PARTY';
export type ClaimStatus = 'SUBMITTED' | 'ASSIGNED' | 'APPROVED' | 'REJECTED' | 'SETTLED';
export type VehicleApplicationStatus = 'UNDER_REVIEW' | 'ASSIGNED' | 'APPROVED' | 'REJECTED' | 'PAID';

export const VEHICLE_CATEGORIES = [
    { key: 'CAR', label: 'Car', color: 'blue' },
    { key: 'EV_CAR', label: 'EV Car', color: 'emerald' },
    { key: 'TWO_WHEELER', label: '2 Wheeler', color: 'violet' },
    { key: 'EV_TWO_WHEELER', label: 'EV 2 Wheeler', color: 'teal' },
    { key: 'AUTO', label: 'Auto / 3 Wheeler', color: 'amber' },
    { key: 'EV_AUTO', label: 'EV Auto', color: 'lime' },
    { key: 'HEAVY_VEHICLE', label: 'Heavy Vehicle', color: 'orange' },
    { key: 'ALL', label: 'All Vehicles', color: 'slate' },
] as const;

export interface PolicyPlan {
    planId: number;
    planName: string;
    policyType: string;
    description: string;
    basePremium: number;
    maxCoverageAmount: number;
    policyDurationMonths: number;
    deductibleAmount: number;
    coversThirdParty: boolean;
    coversOwnDamage: boolean;
    coversTheft: boolean;
    coversNaturalDisaster: boolean;
    zeroDepreciationAvailable: boolean;
    engineProtectionAvailable: boolean;
    roadsideAssistanceAvailable: boolean;
    applicableVehicleType: string;
    active: boolean;
    createdAt?: string;
}

export interface CreatePolicyPlanRequest {
    planName: string;
    policyType: string;
    description: string;
    basePremium: number;
    maxCoverageAmount: number;
    policyDurationMonths: number;
    deductibleAmount: number;
    coversThirdParty: boolean;
    coversOwnDamage: boolean;
    coversTheft: boolean;
    coversNaturalDisaster: boolean;
    zeroDepreciationAvailable: boolean;
    engineProtectionAvailable: boolean;
    roadsideAssistanceAvailable: boolean;
    applicableVehicleType: string;
}

export interface Policy {
    policyId: number;
    policyNumber: string;
    status: PolicyStatus;
    startDate: string;
    endDate: string;
    premiumAmount: number;
    customerId: number;
    customerName?: string;
    underwriterId?: number;
    underwriterName?: string;
    vehicleId: number;
    vehicleRegistrationNumber?: string;
    planId: number;
    planName?: string;
    policyType?: string;
    description?: string;
    maxCoverageAmount?: number;
    deductibleAmount?: number;
    coversThirdParty?: boolean;
    coversOwnDamage?: boolean;
    coversTheft?: boolean;
    coversNaturalDisaster?: boolean;
}

export interface Claim {
    claimId: number;
    claimNumber: string;
    claimType: string;
    status: ClaimStatus;
    approvedAmount?: number;
    policyId: number;
    policyNumber?: string;
    customerId: number;
    customerName?: string;
    claimsOfficerId?: number;
    claimsOfficerName?: string;
    document1Path?: string;
    document2Path?: string;
    document3Path?: string;
    rejectionReason?: string;
    bankAccountNumber?: string;
    ifscCode?: string;
    accountHolderName?: string;
}

export interface Payment {
    paymentId: number;
    amount: number;
    paymentDate: string;
    status: PaymentStatus;
    transactionReference: string;
    policyId: number;
    policyNumber?: string;
    policyType?: string;
    vehicleType?: string;
    customerName?: string;
    customerEmail?: string;
}

export interface ClaimsPayment {
    paymentId: number;
    claimId: number;
    claimNumber: string;
    policyNumber: string;
    policyType: string;
    vehicleType: string;
    amountPaid: number;
    paymentDate: string;
    paymentStatus: string;
    transactionReference: string;
    adminName: string;
    customerName: string;
    customerEmail: string;
    claimsOfficerName: string;
    claimsOfficerEmail: string;
    bankAccountNumber?: string;
    ifscCode?: string;
    accountHolderName?: string;
}

export interface CreateStaffRequest {
    username: string;
    email: string;
    password: string;
    phoneNumber: string;
    fullName: string;
    role: 'UNDERWRITER' | 'CLAIMS_OFFICER';
}

export interface StaffResponse {
    id: number;
    username: string;
    email: string;
    phoneNumber: string;
    fullName: string;
    role: string;
    active: boolean;
    createdAt: string;
}

export interface VehicleApplication {
    vehicleApplicationId: number;
    vehicleOwnerName: string;
    registrationNumber: string;
    make: string;
    model: string;
    year: number;
    fuelType: string;
    chassisNumber: string;
    distanceDriven: number;
    vehicleType: string;
    exShowroomPrice: number;
    idv: number;
    calculatedPremium: number;
    tenureYears: number;
    status: VehicleApplicationStatus;
    rejectionReason?: string;
    createdAt?: Date;
    planId?: number;
    planName?: string;
    policyType?: string;
    description?: string;
    basePremium?: number;
    customerName?: string;
    customerEmail?: string;
    // Assigned Underwriter
    assignedUnderwriterId?: number;
    assignedUnderwriterName?: string;
    rcDocumentPath?: string;
    invoiceDocumentPath?: string;
    policyId?: number;
}

export interface UnderwriterWorkload {
    id: number;
    username: string;
    email: string;
    activeApplicationsCount: number;
}

export interface ClaimsOfficerWorkload {
    id: number;
    username: string;
    email: string;
    activeClaimsCount: number;
}


export interface UpdateApplicationStatusRequest {
    status: VehicleApplicationStatus;
    rejectionReason?: string;
}
