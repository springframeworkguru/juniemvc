/**
 * Beer Order-related type definitions
 */

/**
 * Beer Order Line DTO interface
 * Represents a line item in a beer order
 */
export interface BeerOrderLineDto {
  id?: number;
  version?: number;
  createdDate?: string;
  updateDate?: string;
  beerId: number;
  beerName: string;
  beerStyle: string;
  upc: string;
  orderQuantity: number;
  quantityAllocated?: number;
  status?: string;
}

/**
 * Beer Order Shipment DTO interface
 * Represents a shipment for a beer order
 */
export interface BeerOrderShipmentDto {
  id?: number;
  version?: number;
  createdDate?: string;
  updateDate?: string;
  shipmentDate: string;
  carrier?: string;
  trackingNumber?: string;
}

/**
 * Beer Order DTO interface
 * Represents a beer order entity in the system
 */
export interface BeerOrderDto {
  id?: number;
  version?: number;
  createdDate?: string;
  updateDate?: string;
  /** Nested customer returned/required by the API */
  customer?: import('./customer').CustomerDto;
  /** Legacy/UI helper field (not always present on API responses) */
  customerRef?: string;
  paymentAmount: number;
  status?: string;
  beerOrderLines: BeerOrderLineDto[];
  shipments?: BeerOrderShipmentDto[];
}

/**
 * Beer Order Patch DTO interface
 * Used for partial updates to a beer order entity
 */
export interface BeerOrderPatchDto {
  id?: number;
  version?: number;
  createdDate?: string;
  updateDate?: string;
  customerRef?: string;
  paymentAmount?: number;
  status?: string;
  beerOrderLines?: BeerOrderLineDto[];
  shipments?: BeerOrderShipmentDto[];
}

/**
 * Page of Beer Order DTO interface
 * Represents a paginated list of beer orders
 */
export type PageOfBeerOrderDto = Page<BeerOrderDto>;

/**
 * Import the Page interface from beer.ts
 * This is a workaround until we move the Page interface to a common location
 */
import type { Page } from './beer';
