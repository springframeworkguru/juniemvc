import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@components/ui';
import customerService from '../../services/customerService';
import beerService from '../../services/beerService';
import beerOrderService from '../../services/beerOrderService';
import type { BeerOrderDto, BeerOrderLineDto } from '../../types/beerOrder';

// Local interfaces for form data
interface CustomerOption {
  id: number;
  name: string;
  email: string;
}

interface BeerOption {
  id: number;
  name: string;
  style: string;
  price: number;
  quantityOnHand: number;
}

interface LineItem {
  id: number;
  beerId: string | number;
  beerName: string;
  quantity: number;
  price: number;
}

/**
 * Beer Order Create page component
 * Allows users to create a new beer order
 */
const BeerOrderCreatePage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [customers, setCustomers] = useState<CustomerOption[]>([]);
  const [beers, setBeers] = useState<BeerOption[]>([]);
  const [selectedCustomerId, setSelectedCustomerId] = useState('');
  const [customerRef, setCustomerRef] = useState('');
  const [lineItems, setLineItems] = useState<LineItem[]>([
    { id: 1, beerId: '', beerName: '', quantity: 1, price: 0 },
  ]);

  // Load customers and beers from the API (persisted data)
  useEffect(() => {
    let cancelled = false;

    const loadOptions = async () => {
      try {
        const [customerList, beerPage] = await Promise.all([
          customerService.getCustomers(),
          beerService.getBeers({ page: 0, size: 1000 }),
        ]);

        if (cancelled) {
          return;
        }

        setCustomers(
          customerList
            .filter(c => c.id != null)
            .map(c => ({
              id: c.id as number,
              name: c.name,
              email: c.email || '',
            }))
        );

        setBeers(
          (beerPage.content || [])
            .filter(b => b.id != null)
            .map(b => ({
              id: b.id as number,
              name: b.beerName,
              style: b.beerStyle,
              price: typeof b.price === 'number' ? b.price : Number(b.price),
              quantityOnHand: b.quantityOnHand ?? 0,
            }))
        );
      } catch (error) {
        console.error('Failed to load customers/beers for order form', error);
        if (!cancelled) {
          setCustomers([]);
          setBeers([]);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void loadOptions();

    return () => {
      cancelled = true;
    };
  }, []);

  const handleCustomerChange = (customerId: string) => {
    setSelectedCustomerId(customerId);

    // Generate a customer reference based on the selected customer
    // In a real app, this might be more sophisticated or handled by the backend
    const customer = customers.find(c => c.id.toString() === customerId);
    if (customer) {
      const ref = `CUST-${customer.id}-${Date.now().toString().slice(-4)}`;
      setCustomerRef(ref);
    }
  };

  const handleBeerChange = (beerId: string, index: number) => {
    const beer = beers.find(b => b.id.toString() === beerId);

    if (beer) {
      const updatedLineItems = [...lineItems];
      updatedLineItems[index] = {
        ...updatedLineItems[index],
        beerId: beer.id,
        beerName: beer.name,
        price: beer.price,
      };
      setLineItems(updatedLineItems);
    }
  };

  const handleQuantityChange = (quantity: string, index: number) => {
    const updatedLineItems = [...lineItems];
    updatedLineItems[index] = {
      ...updatedLineItems[index],
      quantity: parseInt(quantity) || 0,
    };
    setLineItems(updatedLineItems);
  };

  const addLineItem = () => {
    setLineItems([
      ...lineItems,
      { id: lineItems.length + 1, beerId: '', beerName: '', quantity: 1, price: 0 },
    ]);
  };

  const removeLineItem = (index: number) => {
    if (lineItems.length > 1) {
      const updatedLineItems = lineItems.filter((_, i) => i !== index);
      setLineItems(updatedLineItems);
    }
  };

  const calculateTotal = () => {
    return lineItems.reduce((total, item) => {
      return total + item.price * item.quantity;
    }, 0);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!selectedCustomerId || lineItems.some(item => !item.beerId)) {
      alert('Please select a customer and beer for each line item');
      return;
    }

    const selectedCustomer = customers.find(c => c.id.toString() === selectedCustomerId);
    if (!selectedCustomer) {
      alert('Please select a valid customer');
      return;
    }

    setSubmitting(true);

    try {
      const beerOrderLines: BeerOrderLineDto[] = lineItems.map(item => {
        const beer = beers.find(b => b.id.toString() === item.beerId.toString());
        return {
          beerId: Number(item.beerId),
          beerName: beer?.name || item.beerName || '',
          beerStyle: beer?.style || '',
          upc: '',
          orderQuantity: item.quantity,
          quantityAllocated: 0,
          status: 'NEW',
        };
      });

      const payload: BeerOrderDto = {
        customer: {
          id: selectedCustomer.id,
          name: selectedCustomer.name,
          email: selectedCustomer.email,
          addressLine1: '',
          city: '',
          state: '',
          postalCode: '',
        },
        customerRef,
        paymentAmount: calculateTotal(),
        status: 'NEW',
        beerOrderLines,
      };

      await beerOrderService.createBeerOrder(payload);
      navigate('/beer-orders');
    } catch (error) {
      console.error('Failed to create beer order', error);
      alert('Failed to create beer order. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    navigate('/beer-orders');
  };

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center">
        <p className="text-lg">Loading...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">Create New Beer Order</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Customer Information</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="customer">Customer</Label>
                <Select value={selectedCustomerId} onValueChange={handleCustomerChange} required>
                  <SelectTrigger>
                    <SelectValue placeholder="Select a customer" />
                  </SelectTrigger>
                  <SelectContent>
                    {customers.map(customer => (
                      <SelectItem key={customer.id} value={customer.id.toString()}>
                        {customer.name} ({customer.email})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="customerRef">Customer Reference</Label>
                <Input
                  id="customerRef"
                  value={customerRef}
                  onChange={e => setCustomerRef(e.target.value)}
                  placeholder="Customer reference"
                  disabled={!selectedCustomerId}
                  required
                />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Order Items</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {lineItems.map((item, index) => (
                <div key={index} className="grid gap-4 rounded-md border p-4 md:grid-cols-4">
                  <div className="space-y-2">
                    <Label htmlFor={`beer-${index}`}>Beer</Label>
                    <Select
                      value={item.beerId.toString()}
                      onValueChange={value => handleBeerChange(value, index)}
                      required
                    >
                      <SelectTrigger id={`beer-${index}`}>
                        <SelectValue placeholder="Select a beer" />
                      </SelectTrigger>
                      <SelectContent>
                        {beers.map(beer => (
                          <SelectItem key={beer.id} value={beer.id.toString()}>
                            {beer.name} ({beer.style})
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor={`quantity-${index}`}>Quantity</Label>
                    <Input
                      id={`quantity-${index}`}
                      type="number"
                      min="1"
                      value={item.quantity}
                      onChange={e => handleQuantityChange(e.target.value, index)}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Price</Label>
                    <div className="flex h-9 items-center rounded-md border border-input bg-background px-3 text-sm">
                      ${item.price.toFixed(2)}
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Label>Subtotal</Label>
                    <div className="flex h-9 items-center rounded-md border border-input bg-background px-3 text-sm">
                      ${(item.price * item.quantity).toFixed(2)}
                    </div>
                  </div>
                  {lineItems.length > 1 && (
                    <div className="flex items-end md:col-span-4">
                      <button
                        type="button"
                        onClick={() => removeLineItem(index)}
                        className="rounded-md bg-red-500 px-3 py-2 text-sm text-white"
                      >
                        Remove Item
                      </button>
                    </div>
                  )}
                </div>
              ))}

              <div className="flex justify-between">
                <button
                  type="button"
                  onClick={addLineItem}
                  className="rounded-md bg-blue-500 px-4 py-2 text-white"
                >
                  Add Item
                </button>
                <div className="text-xl font-bold">Total: ${calculateTotal().toFixed(2)}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={handleCancel}
            className="rounded-md bg-gray-500 px-4 py-2 text-white"
            disabled={submitting}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="rounded-md bg-primary px-4 py-2 text-primary-foreground"
            disabled={submitting}
          >
            {submitting ? 'Creating Order...' : 'Create Order'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default BeerOrderCreatePage;
