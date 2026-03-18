import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-purchase-grid',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './purchase.component.html',
})
export class PurchaseComponent {

    vehicleOptions = [
        {
            id: 'car',
            title: 'Car Insurance',
            subtitle: 'Comprehensive & Third Party',
            bgColor: 'bg-indigo-50',
            textColor: 'text-indigo-600',
            typeParam: 'CAR'
        },
        {
            id: 'ev-car',
            title: 'EV Car Insurance',
            subtitle: 'Special cover for EV batteries',
            bgColor: 'bg-emerald-50',
            textColor: 'text-emerald-600',
            typeParam: 'EV_CAR'
        },
        {
            id: '2-wheeler',
            title: '2-Wheeler Insurance',
            subtitle: 'Bike & Scooter protection',
            bgColor: 'bg-rose-50',
            textColor: 'text-rose-600',
            typeParam: 'TWO_WHEELER'
        },
        {
            id: 'ev-2-wheeler',
            title: 'EV 2-Wheeler',
            subtitle: 'Electric Scooter insurance',
            bgColor: 'bg-teal-50',
            textColor: 'text-teal-600',
            typeParam: 'EV_TWO_WHEELER'
        },
        {
            id: 'auto',
            title: 'Commercial Auto',
            subtitle: 'Auto-rickshaws & 3-wheelers',
            bgColor: 'bg-amber-50',
            textColor: 'text-amber-600',
            typeParam: 'AUTO'
        },
        {
            id: 'truck',
            title: 'Truck & Heavy',
            subtitle: 'Heavy commercial vehicles',
            bgColor: 'bg-slate-100',
            textColor: 'text-slate-600',
            typeParam: 'HEAVY_VEHICLE'
        },
        {
            id: 'ev-auto',
            title: 'EV Auto',
            subtitle: 'Electric 3-wheelers',
            bgColor: 'bg-blue-50',
            textColor: 'text-blue-600',
            typeParam: 'EV_AUTO'
        }
    ];

}
