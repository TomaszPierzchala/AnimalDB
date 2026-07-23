import { useEffect, useState } from 'react';

import ErrorBanner from '../components/ErrorBanner';
import { firstCapital } from '../utils/textUtils';
import { FIRST, SECOND } from '../utils/const';
import DoubleParamForm from './DoubleParamForm';
import TwoColumnTable from './TwoColumnTable';
import { validateRequiredAndMaxLength, validateNonLessThenZeroAndRequiredAndMaxLength } from './textValidation';
import { VAR_MAX_LENGTH, MAX_TARANSLINE_NAME } from './textValidation';

import './View.css';

function TwoColumnView({
  entityName,
  firstName,
  secondName,
  warningKey = FIRST | SECOND,/* int -> binary contains information about warnings e.g. 3 = b11 means warning for 1st &2nd field */

  firstRequestName = firstName,
  firstEditName = firstName,
  firstInputType = 'text',
  getFirstOptionsApi,

  createApi,
  getApi,
  updateApi,
  deleteApi
}) {

  const firstValueSelect = (firstInputType === 'select') ? -1 : '';
  const [records, setRecords] = useState([]);
  const [firstOptions, setFirstOptions] = useState([]);

  const [error, setError] = useState('');
  const [errorFading, setErrorFading] = useState(false);
  const [refreshAfterError, setRefreshAfterError] = useState(false);

  const [popupOpen, setPopupOpen] = useState(false);
  const [entity, setEntity] = useState(null);
  const [deleteArmed, setDeleteArmed] = useState(false);

  const [firstValue, setFirstValue] = useState(firstValueSelect);
  const [secondValue, setSecondValue] = useState('');
  const [fieldWarning, setFieldWarning] = useState('');

  useEffect(() => {
    loadRecords();
  }, [getApi]);

  useEffect(() => {
    if (!getFirstOptionsApi) {
      return;
    }

    async function loadFirstOptions() {
      try {
        const data = await getFirstOptionsApi();

        const options = Array.isArray(data)
          ? data.map(item => ({
              value: item.id,
              label: item.code
            }))
          : [];

        setFirstOptions(options);
      } catch (err) {
        showError(
          'Could not load available options.\n' +
          err.message
        );
      }
    }

    loadFirstOptions();
  }, [getFirstOptionsApi]);

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
    const initialFirstValue = firstValueSelect;
    const initialSecondValue = '';

    setEntity(null);
    setFirstValue(initialFirstValue);
    setSecondValue(initialSecondValue);
    setDeleteArmed(false);

    setFieldWarning(createFieldWarning(initialFirstValue, initialSecondValue));

    setPopupOpen(true);
  }

  function openEditPopup(selectedEntity) {
    const editFirstValue = String(selectedEntity[firstEditName] ?? '');

    const editSecondValue =  selectedEntity[secondName] ?? '';

    setEntity(selectedEntity);
    setFirstValue(editFirstValue);
    setSecondValue(editSecondValue);
    setDeleteArmed(false);

    setFieldWarning(createFieldWarning(editFirstValue, editSecondValue));

    setPopupOpen(true);
  }

  function closePopup() {
    setPopupOpen(false);
    setEntity(null);
    setFirstValue(firstValueSelect);
    setSecondValue('');
    setDeleteArmed(false);
    setFieldWarning('');
  }

  async function refreshAfterPopup() {
    closePopup();
    await loadRecords();
  }

  async function handleSubmit(event) {
    event.preventDefault();

	const validationMessage =
	    createFieldWarning(firstValue, secondValue);

    if (validationMessage) {
      setFieldWarning(validationMessage);
      return;
    }

    if (entity !== null && !hasChanges) {
      return;
    }

    const requestFirstValue =
      firstInputType === 'select'
        ? Number(firstValue)
        : firstValue;

    const requestBody = {
      [firstRequestName]: requestFirstValue,
      [secondName]: secondValue
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

  function createFieldWarning(first, second){
	const maxLength = (entityName==='Transgenic Line') ?  MAX_TARANSLINE_NAME : VAR_MAX_LENGTH;
	const firstWarning =
	  (warningKey & FIRST) !== 0
	    ? firstInputType === 'select'
	      ? validateNonLessThenZeroAndRequiredAndMaxLength(first, maxLength)
	      : validateRequiredAndMaxLength(first, maxLength)
	    : '';

	const secondWarning =
	  (warningKey & SECOND) !== 0
	    ? validateRequiredAndMaxLength(second, maxLength)
	    : '';

	const validationMessage =
	  firstWarning && secondWarning
	    ? 'Please correct both field values.'
	    : firstWarning || secondWarning;

	return validationMessage;
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

  const originalFirstValue =
    entity === null
      ? ''
      : String(entity[firstEditName] ?? '');

  const originalSecondValue =
    entity === null
      ? ''
      : entity[secondName] ?? '';

  const hasChanges =
    entity === null ||
    firstValue !== originalFirstValue ||
    secondValue !== originalSecondValue;

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
          firstInputType={firstInputType}
          firstOptions={firstOptions}

          secondName={firstCapital(secondName)}
          secondValue={secondValue}

          warning={fieldWarning}
          deleteArmed={deleteArmed}
          hasChanges={hasChanges}

          createFieldWarning={createFieldWarning}
          onChangeFirstField={
            (value, warning) => {
              setFirstValue(value);
              (warningKey & FIRST) && setFieldWarning(warning);
            }
          }

          onChangeSecondField={
            (value, warning) => {
              setSecondValue(value);
              (warningKey & SECOND) && setFieldWarning(warning);
            }
          }

          onSubmit={handleSubmit}
          onDelete={handleDelete}
          onCancel={closePopup}
        />
      )}
    </section>
  );
}

export default TwoColumnView;
