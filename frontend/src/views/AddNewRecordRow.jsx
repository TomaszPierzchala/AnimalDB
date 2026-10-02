function AddNewRecordRow({
	entityName,
	onCreate,
	colSpan = 2
}) {
	return 	(
	<tr
	  className="empty-row"
	  onClick={onCreate}
	>
	  <td className="id-column add-icon-cell">
	    <span className="add-icon">+</span>
	  </td>

	  <td
	    colSpan={colSpan}
	    className="add-text-cell"
	  >
	    Click here to add a new {entityName}...
	  </td>
	</tr>
	);
}

export default AddNewRecordRow;