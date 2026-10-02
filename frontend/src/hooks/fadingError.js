import { useEffect, useState } from 'react';

import {
    ERROR_VISIBLE_TIME,
    ERROR_FADE_TIME
} from '../utils/const';

function useFadingError() {
    const [error, setError] = useState('');
    const [errorFading, setErrorFading] = useState(false);
    const [afterError, setAfterError] = useState(null);

    useEffect(() => {
        if (!error) {
            return;
        }

        const fadeTimer = setTimeout(() => {
            setErrorFading(true);
        }, ERROR_VISIBLE_TIME);

        const clearTimer = setTimeout(async () => {
            setError('');
            setErrorFading(false);

            if (afterError) {
                await afterError();
                setAfterError(null);
            }
        }, ERROR_VISIBLE_TIME + ERROR_FADE_TIME);

        return () => {
            clearTimeout(fadeTimer);
            clearTimeout(clearTimer);
        };
    }, [error, afterError]);

    function showError(message, callback = null) {
        setErrorFading(false);
        setError(message);
        setAfterError(() => callback);
    }

    return {
        error,
        errorFading,
        showError
    };
}

export default useFadingError;