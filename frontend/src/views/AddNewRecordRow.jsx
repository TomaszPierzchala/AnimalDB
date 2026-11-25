import {SELECT_TRANSGENIC_LINE} from '../components/MatingCandidatesInput';

const NON_BREAKING_SPACE3 = "\u00A0\u00A0\u00A0"

function AddNewRecordRow({
							 entityName,
							 onCreate,
							 colSpan = 2,
							 acceptText = null,
							 cancelAction = null,
                             onSelect = null,
                             selectOptions = []
						 }) {
	return (
		<tr className={`empty-row${acceptText ? ' accept-row' : ''}`}>

			<td
				className="id-column add-icon-cell"
				onClick={onCreate}
			>
                <span className="add-icon">
                    +
                </span>
			</td>

			<td
				colSpan={colSpan}
				className="add-text-cell"
                onClick={onCreate}
			>
                <div className="accept-container">
                    {(onSelect && cancelAction) && (
                        <select
                            className="accept-select"
                            defaultValue="0"
                            title={"Select the Strain-TransgenicLine.\nThis determines the genetic line of the mating."}
                            onClick={(e) => e.stopPropagation()}
                            onChange={(e) => {
                                onSelect?.(Number(e.target.value));
                            }}
                        >
                            <option value="0" disabled>{SELECT_TRANSGENIC_LINE}</option>
                            {selectOptions.map(option => (
                                <option
                                    key={option.id}
                                    value={option.id}
                                >
                                    {option.strainCode + ' — ' + option.strainName + ' :' + NON_BREAKING_SPACE3 + (option.name?? '—')}
                                </option>
                            ))}
                        </select>
                    )}

                    <span>
                        {acceptText ?? `Click here to add a new ${entityName}...`}
                    </span>
                </div>
            </td>

			{cancelAction && (
				<td className="add-row-action-cell cancel-action"
					onClick={cancelAction}
				> Cancel </td>
			)}

		</tr>
	);
}

export default AddNewRecordRow;