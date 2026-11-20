import AddNewRecordRow from './AddNewRecordRow'
import {SECOND} from "../utils/const.js";

function TwoColumnTable({
  records,
  onEdit,
  onCreate,
  entityName,

  firstName,
  firstName2,
  firstLabel,

  secondName,
  secondLabel,

  nullAllowedKey
}) {
  function getFirstColumnValue(record) {
    if (!firstName2) {
      return record[firstName];
    }

    return `${record[firstName] ?? ''} — ${record[firstName2] ?? ''}`;
  }
  function longDashWhenNull(value) {
      return value ?? (
          <span className="null-value" title="long dash when null">
            —
          </span>
      )
  }

  return (
    <table>
      <colgroup>
        <col className="id-table-column" />
        <col className="symbol-table-column" />
        <col className="description-table-column" />
      </colgroup>

      <thead>
        <tr>
          <th className="id-column">ID</th>
          <th>{firstLabel}</th>
          <th>{secondLabel}</th>
        </tr>
      </thead>

      <tbody>
        {records.map(record => (
          <tr
            key={record.id}
            className="clickable-row"
            onClick={() => onEdit(record)}
          >
            <td className="id-column">
              {record.id}
            </td>

            <td>
              {(nullAllowedKey & SECOND) !== 0 ? getFirstColumnValue(record) : getFirstColumnValue(record)}
            </td>

            <td>
              {(nullAllowedKey & SECOND) !== 0 ? longDashWhenNull(record[secondName]) : record[secondName]}
            </td>
          </tr>
        ))}
        <AddNewRecordRow
            entityName={entityName}
            onCreate={onCreate}
        />
      </tbody>
    </table>
  );
}

export default TwoColumnTable;
