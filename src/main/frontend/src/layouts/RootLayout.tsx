import { useState } from 'react';
import { Outlet, Link, useLocation } from 'react-router-dom';
import { Button } from '@components/ui';
import { Menu, X, Home, Beer, Users, ShoppingCart } from 'lucide-react';
import ToastContainer from '../components/dialogs/ToastContainer';

/**
 * Root layout component that wraps all pages
 * Includes responsive sidebar navigation, header, and footer
 */
const RootLayout: React.FC = () => {
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Helper function to determine if a link is active
  const isActive = (path: string) => {
    return location.pathname === path || location.pathname.startsWith(path + '/');
  };

  // Navigation items
  const navigationItems = [
    { path: '/', label: 'Home', icon: Home },
    { path: '/beers', label: 'Beers', icon: Beer },
    { path: '/customers', label: 'Customers', icon: Users },
    { path: '/beer-orders', label: 'Beer Orders', icon: ShoppingCart },
  ];

  return (
    <div className="flex min-h-screen bg-gray-50">
      {/* Sidebar */}
      <aside
        className={`
        fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform transition-transform duration-300 ease-in-out
        lg:translate-x-0 lg:static lg:inset-0
        ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
      `}
      >
        <div className="flex h-full flex-col">
          {/* Sidebar header */}
          <div className="flex h-16 items-center justify-between px-4 border-b">
            <h1 className="text-xl font-bold text-gray-900">Beer Service</h1>
            <Button
              variant="ghost"
              size="sm"
              className="lg:hidden"
              onClick={() => setSidebarOpen(false)}
            >
              <X className="h-5 w-5" />
            </Button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 px-4 py-6">
            <ul className="space-y-2">
              {navigationItems.map(item => {
                const Icon = item.icon;
                const active = isActive(item.path);

                return (
                  <li key={item.path}>
                    <Link
                      to={item.path}
                      onClick={() => setSidebarOpen(false)}
                      className={`
                        flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors
                        ${
                          active
                            ? 'bg-primary text-primary-foreground'
                            : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                        }
                      `}
                    >
                      <Icon className="h-5 w-5" />
                      {item.label}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </nav>
        </div>
      </aside>

      {/* Sidebar overlay for mobile */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-black bg-opacity-50 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Main content area */}
      <div className="flex flex-1 flex-col lg:ml-0">
        {/* Header */}
        <header className="sticky top-0 z-30 border-b bg-white shadow-sm">
          <div className="flex h-16 items-center justify-between px-4">
            <Button
              variant="ghost"
              size="sm"
              className="lg:hidden"
              onClick={() => setSidebarOpen(true)}
            >
              <Menu className="h-5 w-5" />
            </Button>

            <div className="flex items-center gap-4 ml-auto">
              {/* Add user menu, notifications, etc. here in the future */}
            </div>
          </div>
        </header>

        {/* Main content */}
        <main className="flex-1 p-6">
          <Outlet />
        </main>

        {/* Footer */}
        <footer className="border-t bg-white py-6">
          <div className="px-6 text-center text-sm text-gray-600">
            &copy; {new Date().getFullYear()} Beer Service. All rights reserved.
          </div>
        </footer>
      </div>
      <ToastContainer />
    </div>
  );
};

export default RootLayout;
