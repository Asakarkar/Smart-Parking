import { useCallback, useEffect, useRef, useState } from 'react';
import { getErrorMessage } from '../services/api.js';

/**
 * Runs an API call and tracks { data, loading, error }.
 * Call reload() to fetch again (e.g. after a booking or checkout).
 */
export default function useFetch(fetcher, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const fetcherRef = useRef(fetcher);
  fetcherRef.current = fetcher;

  const reload = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setData(await fetcherRef.current());
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { reload(); }, [reload, ...deps]);

  return { data, loading, error, reload, setData };
}
