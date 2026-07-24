function TwoColumnTable({
  records,
  onEdit,
  onCreate,
  entityName,

  firstName,
  firstName2,
  firstLabel,

  secondName,
  secondLabel
}) {
  function getFirstColumnValue(record) {
    if (!firstName2) {
      return record[firstName];
    }

    return `${record[firstName] ?? ''} - ${record[firstName2] ?? ''}`;
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
              {getFirstColumnValue(record)}
            </td>

            <td>
              {record[secondName]}
            </td>
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
