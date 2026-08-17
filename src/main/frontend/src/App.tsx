import React, { Suspense } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { RootLayout } from './layouts';
import { AuthProvider } from './contexts/AuthContext';
import { ToastProvider } from './contexts/ToastContext';

// Lazy load page components for code splitting
const HomePage = React.lazy(() => import('./pages/HomePage'));
const LoginPage = React.lazy(() => import('./pages/auth/LoginPage'));

// Beer pages
const BeerListPage = React.lazy(() => import('./pages/beers/BeerListPage'));
const BeerDetailPage = React.lazy(() => import('./pages/beers/BeerDetailPage'));
const BeerCreatePage = React.lazy(() => import('./pages/beers/BeerCreatePage'));
const BeerEditPage = React.lazy(() => import('./pages/beers/BeerEditPage'));

// Customer pages
const CustomerListPage = React.lazy(() => import('./pages/customers/CustomerListPage'));
const CustomerDetailPage = React.lazy(() => import('./pages/customers/CustomerDetailPage'));
const CustomerCreatePage = React.lazy(() => import('./pages/customers/CustomerCreatePage'));
const CustomerEditPage = React.lazy(() => import('./pages/customers/CustomerEditPage'));

// Beer Order pages
const BeerOrderListPage = React.lazy(() => import('./pages/beerOrders/BeerOrderListPage'));
const BeerOrderDetailPage = React.lazy(() => import('./pages/beerOrders/BeerOrderDetailPage'));
const BeerOrderCreatePage = React.lazy(() => import('./pages/beerOrders/BeerOrderCreatePage'));
const BeerOrderEditPage = React.lazy(() => import('./pages/beerOrders/BeerOrderEditPage'));
const BeerOrderShipmentCreatePage = React.lazy(
  () => import('./pages/beerOrders/BeerOrderShipmentCreatePage')
);

// Loading component for Suspense fallback
const LoadingSpinner: React.FC = () => (
  <div className="flex items-center justify-center min-h-screen">
    <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary"></div>
  </div>
);

/**
 * Main application component
 * Sets up the routing structure for the application
 */
const App: React.FC = () => {
  // Create router configuration
  const router = createBrowserRouter([
    {
      path: '/login',
      element: (
        <Suspense fallback={<LoadingSpinner />}>
          <LoginPage />
        </Suspense>
      ),
    },
    {
      path: '/',
      element: <RootLayout />,
      children: [
        {
          index: true,
          element: (
            <Suspense fallback={<LoadingSpinner />}>
              <HomePage />
            </Suspense>
          ),
        },
        {
          path: 'beers',
          children: [
            {
              index: true,
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <BeerListPage />
                </Suspense>
              ),
            },
            {
              path: 'new',
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <BeerCreatePage />
                </Suspense>
              ),
            },
            {
              path: ':beerId',
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <BeerDetailPage />
                </Suspense>
              ),
            },
            {
              path: ':beerId/edit',
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <BeerEditPage />
                </Suspense>
              ),
            },
          ],
        },
        {
          path: 'customers',
          children: [
            {
              index: true,
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <CustomerListPage />
                </Suspense>
              ),
            },
            {
              path: 'new',
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <CustomerCreatePage />
                </Suspense>
              ),
            },
            {
              path: ':customerId',
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <CustomerDetailPage />
                </Suspense>
              ),
            },
            {
              path: ':customerId/edit',
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <CustomerEditPage />
                </Suspense>
              ),
            },
          ],
        },
        {
          path: 'beer-orders',
          children: [
            {
              index: true,
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <BeerOrderListPage />
                </Suspense>
              ),
            },
            {
              path: 'new',
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <BeerOrderCreatePage />
                </Suspense>
              ),
            },
            {
              path: ':beerOrderId',
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <BeerOrderDetailPage />
                </Suspense>
              ),
            },
            {
              path: ':beerOrderId/edit',
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <BeerOrderEditPage />
                </Suspense>
              ),
            },
            {
              path: ':beerOrderId/shipments/new',
              element: (
                <Suspense fallback={<LoadingSpinner />}>
                  <BeerOrderShipmentCreatePage />
                </Suspense>
              ),
            },
          ],
        },
      ],
    },
  ]);

  return (
    <AuthProvider>
      <ToastProvider>
        <RouterProvider router={router} />
      </ToastProvider>
    </AuthProvider>
  );
};

export default App;
