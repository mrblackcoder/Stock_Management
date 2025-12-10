import { render } from '@testing-library/react';
import App from './App';

// Mock the service/Guard module
jest.mock('./service/Guard', () => ({
  ProtectedRoute: ({ children }) => <div data-testid="protected">{children}</div>,
  AdminRoute: ({ children }) => <div data-testid="admin">{children}</div>,
  PublicRoute: ({ children }) => <div data-testid="public">{children}</div>,
}));

// Mock the page components
jest.mock('./pages/LoginPage', () => () => <div>Login Page</div>);
jest.mock('./pages/RegisterPage', () => () => <div>Register Page</div>);
jest.mock('./pages/DashboardPage', () => () => <div>Dashboard Page</div>);
jest.mock('./pages/ProductPage', () => () => <div>Product Page</div>);
jest.mock('./pages/CategoryPage', () => () => <div>Category Page</div>);
jest.mock('./pages/SupplierPage', () => () => <div>Supplier Page</div>);
jest.mock('./pages/TransactionPage', () => () => <div>Transaction Page</div>);
jest.mock('./pages/ProfilePage', () => () => <div>Profile Page</div>);
jest.mock('./components/Layout', () => ({ children }) => <div data-testid="layout">{children}</div>);

test('renders app without crashing', () => {
  render(<App />);
});
