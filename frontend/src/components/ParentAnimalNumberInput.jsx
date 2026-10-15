import {useState} from 'react';
import {getParentCandidates} from '../api/miceApi';

function ParentAnimalNumberInput({
                                     value,
                                     sex,
                                     onChange
                                 }) {
    const [candidates, setCandidates] = useState([]);

    async function handleChange(event) {
        const text = event.target.value;

        onChange(
            text === ''
                ? null
                : Number(text)
        );

        if (text.length < 1) {
            setCandidates([]);
            return;
        }

        try {
            const result =
                await getParentCandidates(text, sex);

            setCandidates(result);
        } catch {
            setCandidates([]);
        }
    }

    return (
        <>
            <input
                type="number"
                value={value ?? ''}
                list={`parent-${sex}-candidates`}
                onClick={event => event.stopPropagation()}
                onChange={handleChange}
            />

            <datalist id={`parent-${sex}-candidates`}>
                {candidates.map(candidate => (
                    <option
                        key={candidate.animalNumber}
                        value={candidate.animalNumber}
                    />
                ))}
            </datalist>
        </>
    );
}

export default ParentAnimalNumberInput;