import { VAR_MAX_LENGTH } from './textValidation';

function DoubleParamForm({
  entity,
  entityName,

  firstName,
  firstValue,
  firstInputType,
  firstOptions,

  secondName,
  secondValue,
  secondMaxLength,

  warning,
  deleteArmed,
  hasChanges,

  createFieldWarning,
  onChangeFirstField,
  onChangeSecondField,
  onSubmit,
  onDelete,
  onCancel
}) {
  const editing = entity !== null;

  return (
    <div className="popup-backdrop">
      <div className="popup">
        <h2>
          {editing
            ? `Edit ${entityName}`
            : `Add ${entityName}`}
        </h2>

        <form onSubmit={onSubmit}>
          <div>
            <label>
              {firstName}:

              {firstInputType === 'select' ? (
                <select
                  className="strain-select"
                  value={firstValue}
                  onChange={event => {
                    const value = event.target.value;

                    onChangeFirstField(
                      value,
                      createFieldWarning(
                        value,
                        secondValue
                      )
                    );
                  }}
                >
                  <option value="-1">
                    Select from drop-down menu
                  </option>

                  {firstOptions.map(option => (
                    <option
                      key={option.value}
                      value={option.value}
                    >
                      {option.label}
                    </option>
                  ))}
                </select>
              ) : (
                <input
                  type="text"
                  value={firstValue}
                  maxLength={VAR_MAX_LENGTH}
                  onChange={event => {
                    const value = event.target.value;

                    onChangeFirstField(
                      value,
                      createFieldWarning(
                        value,
                        secondValue
                      )
                    );
                  }}
                />
              )}

              <small className="field-warning pulsing-text">
                {warning}
              </small>
            </label>
          </div>

          <div>
            <label>
              {secondName}:

              <input
                type="text"
                value={secondValue}
                maxLength={secondMaxLength}
                onChange={event => {
                  const value = event.target.value;

                  onChangeSecondField(
                    value,
                    createFieldWarning(
                      firstValue,
                      value
                    )
                  );
                }}
              />
            </label>
          </div>

          <div className="popup-buttons">
            <div>
              {editing && (
                <button
                  type="button"
                  className={
                    deleteArmed
                      ? 'delete-button delete-armed'
                      : 'delete-button'
                  }
                  onClick={onDelete}
                >
                  {deleteArmed
                    ? 'Confirm delete'
                    : 'Delete'}
                </button>
              )}
            </div>

            <div className="popup-main-buttons">
              <button
                type="submit"
                disabled={editing && !hasChanges}
              >
                {editing ? 'Save' : 'Add'}
              </button>

              <button
                type="button"
                onClick={onCancel}
              >
                Cancel
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

export default DoubleParamForm;
