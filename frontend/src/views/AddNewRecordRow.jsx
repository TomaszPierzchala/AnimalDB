function AddNewRecordRow({
	entityName,
	onCreate
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
	    colSpan={2}
	    className="add-text-cell"
	  >
	    Click here to add a new {entityName}...
	  </td>
	</tr>
	);
}

export default AddNewRecordRow;