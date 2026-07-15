function GeneTable({
  genes,
  onEdit,
  onCreate
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
          <th>Symbol</th>
          <th>Description</th>
        </tr>
      </thead>

      <tbody>
        {genes.map((gene) => (
          <tr
            key={gene.id}
            className="clickable-row"
            onClick={() => onEdit(gene)}
          >
            <td className="id-column">{gene.id}</td>
            <td>{gene.symbol}</td>
            <td>{gene.description}</td>
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
            Click here to add a new gene...
          </td>
        </tr>
      </tbody>
    </table>
  );
}

export default GeneTable;
