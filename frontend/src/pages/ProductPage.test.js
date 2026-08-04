import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import ProductPage from './ProductPage';
import ApiService from '../service/ApiService';

/**
 * Proves the page actually routes failures through the shared helper.
 *
 * ApiService is stubbed, but ../service/apiError is deliberately NOT mocked: if the
 * page reverted to `err.message`, this test would see axios's
 * "Request failed with status code 409" instead of the backend's domain message.
 */
jest.mock('../service/ApiService', () => ({
    __esModule: true,
    default: {
        getRole: jest.fn(),
        getUsername: jest.fn(),
        getAllProducts: jest.fn(),
        getAllCategories: jest.fn(),
        getAllSuppliers: jest.fn(),
        deleteProduct: jest.fn(),
        logout: jest.fn()
    }
}));

const PRODUCT_HISTORY_MESSAGE =
    'Product cannot be deleted because it has stock transaction history.';

const conflict = (message) => {
    const error = new Error('Request failed with status code 409');
    error.isAxiosError = true;
    error.response = { status: 409, data: { statusCode: 409, message }, headers: {} };
    return error;
};

const renderPage = () => render(
    <MemoryRouter>
        <ProductPage />
    </MemoryRouter>
);

describe('ProductPage', () => {
    beforeEach(() => {
        jest.clearAllMocks();

        // The page fetches live exchange rates on mount; keep it off the network.
        global.fetch = jest.fn(() =>
            Promise.resolve({ json: () => Promise.resolve({ rates: { USD: 0.03, EUR: 0.03 } }) })
        );
        jest.spyOn(window, 'confirm').mockReturnValue(true);

        ApiService.getRole.mockReturnValue('ADMIN');
        ApiService.getUsername.mockReturnValue('admin');
        ApiService.getAllProducts.mockResolvedValue({
            statusCode: 200,
            productList: [{ id: 1, name: 'Laptop', sku: 'LAP-001', price: 100, stockQuantity: 5, categoryName: 'Computers' }],
            page: 0,
            size: 10,
            totalPages: 1,
            totalElements: 1
        });
        ApiService.getAllCategories.mockResolvedValue({ statusCode: 200, categoryList: [] });
        ApiService.getAllSuppliers.mockResolvedValue({ statusCode: 200, supplierList: [] });
    });

    afterEach(() => {
        jest.restoreAllMocks();
        delete global.fetch;
    });

    test('showsBackendConflictMessageWhenDeletionIsRejected', async () => {
        ApiService.deleteProduct.mockRejectedValue(conflict(PRODUCT_HISTORY_MESSAGE));

        renderPage();
        expect(await screen.findByText('Laptop')).toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', { name: 'Sil' }));

        await waitFor(() => {
            expect(screen.getByText(PRODUCT_HISTORY_MESSAGE)).toBeInTheDocument();
        });
        expect(screen.queryByText(/Request failed with status code/)).not.toBeInTheDocument();
    });

    test('fallsBackToThePageMessageWhenTheFailureCarriesNoBackendMessage', async () => {
        const networkFailure = new Error('Network Error');
        networkFailure.isAxiosError = true;
        ApiService.deleteProduct.mockRejectedValue(networkFailure);

        renderPage();
        expect(await screen.findByText('Laptop')).toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', { name: 'Sil' }));

        await waitFor(() => {
            expect(screen.getByText('Network Error')).toBeInTheDocument();
        });
    });
});
