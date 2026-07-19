import { firstCapital } from '../utils/textUtils';

function TwoColumnTable({
  records,
  onEdit,
  onCreate,
  entityName, firstName, secondName
}) {
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
          <th>{firstCapital(firstName)}</th>
          <th>{firstCapital(secondName)}</th>
        </tr>
      </thead>

      <tbody>
        {records.map((record) => (
          <tr
            key={record.id}
            className="clickable-row"
            onClick={() => onEdit(record)}
          >
            <td className="id-column">{record.id}</td>
            <td>{record[firstName]}</td>
            <td>{record[secondName]}</td>
          </tr>
        ))}

        <tr
          className="empty-row"
          onClick={onCreate}
        >
          <td className="id-column add-icon-cell">
            <span className="add-icon">+</span>
          </td>

          <td
            colSpan={2}
            className="add-text-cell"
          >
            Click here to add a new {entityName}...
          </td>
        </tr>
      </tbody>
    </table>
  );
}

export default TwoColumnTable;
