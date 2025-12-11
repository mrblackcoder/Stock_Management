import { render } from '@testing-library/react';
import App from './App';

// Smoke test - verifies App renders without crashing
test('renders without crashing', () => {
  // App uses Router which requires window.location
  // This test ensures the basic component structure is valid
  const { container } = render(<App />);
  expect(container).toBeInTheDocument();
});
