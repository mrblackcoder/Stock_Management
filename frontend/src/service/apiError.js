/**
 * Turns an axios failure into the message a user should actually read.
 *
 * The backend already decides what is safe to disclose - domain rules like
 * "Product cannot be deleted because it has stock transaction history." and
 * deliberately generic ones like "An unexpected error occurred." Reading only
 * error.message threw all of that away and showed the caller axios's own
 * "Request failed with status code 409" instead.
 */

const asNonEmptyString = (value) =>
    typeof value === "string" && value.trim() ? value.trim() : "";

/**
 * Renders a field-error payload, whether the backend sends a map of
 * field -> message or a plain list of messages.
 */
const formatValidationDetails = (errors) => {
    if (!errors) return "";

    if (Array.isArray(errors)) {
        return errors.map(asNonEmptyString).filter(Boolean).join("; ");
    }

    if (typeof errors === "object") {
        return Object.entries(errors)
            .map(([field, message]) => {
                const text = asNonEmptyString(message);
                return text ? `${field}: ${text}` : "";
            })
            .filter(Boolean)
            .join("; ");
    }

    return asNonEmptyString(errors);
};

/**
 * Resolution order:
 *   1. the backend's public message
 *   2. field-validation details, when the backend sends them separately
 *   3. the transport message, but only when there was no response at all
 *      (network failure, timeout, DNS) - otherwise this is just axios boilerplate
 *   4. the caller's own fallback
 */
export function extractApiMessage(error, fallback = "Something went wrong. Please try again.") {
    const response = error?.response;

    const backendMessage = asNonEmptyString(response?.data?.message);
    if (backendMessage) return backendMessage;

    const validationDetails = formatValidationDetails(response?.data?.errors);
    if (validationDetails) return validationDetails;

    if (!response) {
        const transportMessage = asNonEmptyString(error?.message);
        if (transportMessage) return transportMessage;
    }

    return fallback;
}

/**
 * True when the server refused the request for authentication or authorization
 * reasons. Reads the real status instead of substring-matching axios's English
 * message, which broke as soon as error.message was absent.
 */
export function isAuthFailure(error) {
    const status = error?.response?.status;
    return status === 401 || status === 403;
}
