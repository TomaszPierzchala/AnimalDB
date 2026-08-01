import TwoColumnView from './TwoColumnView';
import { FIRST } from '../utils/const'


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
      firstLabel="Strain code — strain name"

      secondName="name"
      secondLabel="Name"

      firstRequestName="strainId"
      firstEditName="strainId"
      firstInputType="select"

      dropDown={FIRST}

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
