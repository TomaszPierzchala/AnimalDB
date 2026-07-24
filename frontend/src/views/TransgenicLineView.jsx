import TwoColumnView from './TwoColumnView';

import {
  createTransLine,
  deleteTransLine,
  getTransLines,
  updateTransLine
} from '../api/transgenicLineApi';

import { getStrains } from '../api/strainApi';

function TransgenicLineView() {
  return (
    <TwoColumnView
      entityName="Transgenic Line"

      firstName="strainCode"
      firstName2="strainName"
      firstLabel="Strain code - strain name"

      firstRequestName="strainId"
      firstEditName="strainId"
      firstInputType="select"

      secondName="name"
      secondLabel="Name"

      getSubEntityApi={getStrains}
      subEntityLabelName="code"
      subEntitySecondLabelName="name"

      createApi={createTransLine}
      getApi={getTransLines}
      updateApi={updateTransLine}
      deleteApi={deleteTransLine}
    />
  );
}

export default TransgenicLineView;
