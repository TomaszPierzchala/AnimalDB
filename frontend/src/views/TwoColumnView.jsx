import { useEffect, useState } from 'react';

import ErrorBanner from '../components/ErrorBanner';
import { firstCapital } from '../utils/textUtils';
import { FIRST, SECOND } from '../utils/const';
import DoubleParamForm from './DoubleParamForm';
import TwoColumnTable from './TwoColumnTable';

import {
  MAX_TARANSLINE_NAME,
  VAR_MAX_LENGTH,
  validateNonLessThenZeroAndRequiredAndMaxLength,
  validateRequiredAndMaxLength
} from './textValidation';

import './View.css';

function TwoColumnView({
  entityName,

  firstName,
  firstName2 = null,
  firstLabel = firstCapital(firstName),

  secondName,
  secondLabel = firstCapital(secondName),

  warningKey = FIRST | SECOND,

  firstRequestName = firstName,
  firstEditName = firstName,
  firstInputType = 'text',

  getSubEntityApi,
  subEntityLabelName = 'code',
  subEntitySecondLabelName = null,

  createApi,
  getApi,
  updateApi,
  deleteApi
}) {
  const initialFirstValue =
    firstInputType === 'select' ? '-1' : '';

  const secondMaxLength =
    entityName === 'Transgenic Line'
      ? MAX_TARANSLINE_NAME
      : VAR_MAX_LENGTH;

  const [records, setRecords] = useState([]);
  const [firstOptions, setFirstOptions] = useState([]);

  const [error, setError] = useState('');
  const [errorFading, setErrorFading] = useState(false);
  const [refreshAfterError, setRefreshAfterError] =
    useState(false);

  const [popupOpen, setPopupOpen] = useState(false);
  const [entity, setEntity] = useState(null);
  const [deleteArmed, setDeleteArmed] = useState(false);

  const [firstValue, setFirstValue] =
    useState(initialFirstValue);

  const [secondValue, setSecondValue] = useState('');
  const [fieldWarning, setFieldWarning] = useState('');

  useEffect(() => {
    async function load() {
      await loadRecords();
    }

    load();
  }, [getApi]);

  useEffect(() => {
    if (!getSubEntityApi) {
      return;
    }

    async function loadOptions() {
      try {
        const data = await getSubEntityApi();

        const options = Array.isArray(data)
          ? data.map(item => ({
              value: String(item.id),
              label: subEntitySecondLabelName
                ? `${item[subEntityLabelName]} - ${item[subEntitySecondLabelName]}`
                : item[subEntityLabelName]
            }))
          : [];

        setFirstOptions(options);
      } catch (err) {
        showError(
          `Could not load available options.\n${err.message}`
        );
      }
    }

    loadOptions();
  }, [
    getSubEntityApi,
    subEntityLabelName,
    subEntitySecondLabelName
  ]);

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
      showError(`Could not load data.\n${err.message}`);
    }
  }

  function showError(message) {
    setErrorFading(false);
    setError(message);
  }

  function createFieldWarning(first, second) {
    const firstWarning =
      (warningKey & FIRST) !== 0
        ? firstInputType === 'select'
          ? validateNonLessThenZeroAndRequiredAndMaxLength(
              {
                name: firstLabel,
                value: first
              },
              VAR_MAX_LENGTH
            )
          : validateRequiredAndMaxLength(
              {
                name: firstLabel,
                value: first
              },
              VAR_MAX_LENGTH
            )
        : '';

    const secondWarning =
      (warningKey & SECOND) !== 0
        ? validateRequiredAndMaxLength(
            {
              name: secondLabel,
              value: second
            },
            secondMaxLength
          )
        : '';

    if (firstWarning && secondWarning) {
      return 'Please correct both field values.';
    }

    return firstWarning || secondWarning;
  }

  function openCreatePopup() {
    const first = initialFirstValue;
    const second = '';

    setEntity(null);
    setFirstValue(first);
    setSecondValue(second);
    setDeleteArmed(false);
    setFieldWarning(createFieldWarning(first, second));
    setPopupOpen(true);
  }

  function openEditPopup(selectedEntity) {
    const first = String(
      selectedEntity[firstEditName] ?? initialFirstValue
    );

    const second =
      selectedEntity[secondName] ?? '';

    setEntity(selectedEntity);
    setFirstValue(first);
    setSecondValue(second);
    setDeleteArmed(false);
    setFieldWarning(createFieldWarning(first, second));
    setPopupOpen(true);
  }

  function closePopup() {
    setPopupOpen(false);
    setEntity(null);
    setFirstValue(initialFirstValue);
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

    const requestBody = {
      [firstRequestName]:
        firstInputType === 'select'
          ? Number(firstValue)
          : firstValue,

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
      const operation =
        entity === null ? 'create a new' : 'update the';

      showError(
        `Could not ${operation} ${entityName.toLowerCase()}.\n${err.message}`
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
        `Could not delete the ${entityName.toLowerCase()}.\n${err.message}`
      );

      closePopup();
      setRefreshAfterError(true);
    }
  }

  const originalFirstValue =
    entity === null
      ? initialFirstValue
      : String(entity[firstEditName] ?? initialFirstValue);

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
        firstName2={firstName2}
        firstLabel={firstLabel}
        secondName={secondName}
        secondLabel={secondLabel}
      />

      {popupOpen && (
        <DoubleParamForm
          entity={entity}
          entityName={entityName}
          firstName={firstLabel}
          firstValue={firstValue}
          firstInputType={firstInputType}
          firstOptions={firstOptions}
          secondName={secondLabel}
          secondValue={secondValue}
          secondMaxLength={secondMaxLength}
          warning={fieldWarning}
          deleteArmed={deleteArmed}
          hasChanges={hasChanges}
          createFieldWarning={createFieldWarning}
          onChangeFirstField={(value, warning) => {
            setFirstValue(value);

            if ((warningKey & FIRST) !== 0) {
              setFieldWarning(warning);
            }
          }}
          onChangeSecondField={(value, warning) => {
            setSecondValue(value);

            if ((warningKey & SECOND) !== 0) {
              setFieldWarning(warning);
            }
          }}
          onSubmit={handleSubmit}
          onDelete={handleDelete}
          onCancel={closePopup}
        />
      )}
    </section>
  );
}

export default TwoColumnView;
