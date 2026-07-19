import { useEffect, useState } from 'react';

import ErrorBanner from '../components/ErrorBanner';
import { firstCapital } from '../utils/textUtils';
import DoubleParamForm from './DoubleParamForm';
import TwoColumnTable from './TwoColumnTable';
import { validateEmptyAndMax } from './textValidation';

import './View.css';

function TwoColumnView({
	entityName,
	firstName,
	secondName,
	createApi,
	getApi,
	updateApi,
	deleteApi
}) {
  const [records, setRecords] = useState([]);

  const [error, setError] = useState('');
  const [errorFading, setErrorFading] = useState(false);
  const [refreshAfterError, setRefreshAfterError] = useState(false);

  const [popupOpen, setPopupOpen] = useState(false);
  const [entity, setEntity] = useState(null);
  const [deleteArmed, setDeleteArmed] = useState(false);

  const [firstValue, setFirstValue] = useState('');
  const [secondValue, setSecondField] = useState('');
  const [firstFieldWarning, setFirstFieldWarning] = useState('');

  useEffect(() => {
    loadRecords();
  }, []);

  useEffect(() => {
    if (!error) {
      return;
    }

    const fadeTimer = setTimeout(() => {
      setErrorFading(true);
    }, 3000);

    const clearTimer = setTimeout(async () => {
      setError('');
      setErrorFading(false);

      if (refreshAfterError) {
        setRefreshAfterError(false);
        await loadRecords();
      }
    }, 6500);

    return () => {
      clearTimeout(fadeTimer);
      clearTimeout(clearTimer);
    };
  }, [error, refreshAfterError]);

  async function loadRecords() {
    try {
      const data = await getApi();

      setRecords(Array.isArray(data) ? data : []);
      setError('');
    } catch (err) {
      showError(
        'Could not load data.\n' + err.message
      );
    }
  }

  function showError(message) {
    setErrorFading(false);
    setError(message);
  }

  function openCreatePopup() {
    setEntity(null);
    setFirstValue('');
    setSecondField('');
    setDeleteArmed(false);
    setFirstFieldWarning(validateEmptyAndMax(''));
    setPopupOpen(true);
  }

  function openEditPopup(entity) {
    setEntity(entity);
    setFirstValue(entity[firstName]);
    setSecondField(entity[secondName] ?? '');
    setDeleteArmed(false);
    setFirstFieldWarning(validateEmptyAndMax(entity[firstName]));
    setPopupOpen(true);
  }

  function closePopup() {
    setPopupOpen(false);
    setEntity(null);
    setFirstValue('');
    setSecondField('');
    setDeleteArmed(false);
    setFirstFieldWarning('');
  }

  async function refreshAfterPopup() {
    closePopup();
    await loadRecords();
  }

  async function handleSubmit(event) {
    event.preventDefault();

    const validationMessage = validateEmptyAndMax(firstValue);

    if (firstValue.trim() === '') {
      setFirstFieldWarning(validationMessage);
      return;
    }

    if (entity !== null && !hasChanges) {
      return;
    }

    const requestBody = {
      symbol: firstValue,
      description: secondValue
    };

    try {
      if (entity === null) {
        await createApi(requestBody);
      } else {
        await updateApi(entity.id, requestBody);
      }

      await refreshAfterPopup();
    } catch (err) {
      showError(
        (
          entity === null
            ? `Could not create a new ${entityName.toLowerCase()}.`
            : `Could not update the ${entityName.toLowerCase()}.`
        ) +
        '\n' +
        err.message
      );

      if (entity !== null) {
        closePopup();
        setRefreshAfterError(true);
      }
    }
  }

  async function handleDelete() {
    if (entity === null) {
      return;
    }

    if (!deleteArmed) {
      setDeleteArmed(true);
      return;
    }

    try {
      await deleteApi(entity.id);
      await refreshAfterPopup();
    } catch (err) {
      showError(
        `Could not delete the ${entityName.toLowerCase()}.\n` +
        err.message
      );

      closePopup();
      setRefreshAfterError(true);
    }
  }

  const hasChanges =
    entity === null ||
    firstValue !== entity[firstName] ||
    secondValue !==
      (entity[secondName] ?? '');

  return (
    <section>
      <ErrorBanner
        message={error}
        fading={errorFading}
      />

      <h1>{entityName}s</h1>

      <TwoColumnTable
        records={records}
        onEdit={openEditPopup}
        onCreate={openCreatePopup}
		entityName={entityName}
        firstName={firstName}
        secondName={secondName}
      />

      {popupOpen && (
        <DoubleParamForm
          entity={entity}
          entityName={entityName}
          firstName={firstCapital(firstName)}
          firstValue={firstValue}
          secondName={firstCapital(secondName)}
          secondValue={secondValue}
          warning={firstFieldWarning}
          deleteArmed={deleteArmed}
          hasChanges={hasChanges}
          onChangeWithValidation={(value, warning) => {
            setFirstValue(value);
            setFirstFieldWarning(warning);
          }}
          onChange={setSecondField}
          onSubmit={handleSubmit}
          onDelete={handleDelete}
          onCancel={closePopup}
        />
      )}
    </section>
  );
}

export default TwoColumnView;
