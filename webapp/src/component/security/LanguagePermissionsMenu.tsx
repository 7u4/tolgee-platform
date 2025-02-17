import { ComponentProps, FunctionComponent, useRef, useState } from 'react';
import { Button, styled, Tooltip, Popover, Checkbox } from '@mui/material';
import { ArrowDropDown, CheckBox } from '@mui/icons-material';
import { useTranslate } from '@tolgee/react';

import { LanguagesPermittedList } from 'tg.component/languages/LanguagesPermittedList';
import { SearchSelectMulti } from 'tg.component/searchSelect/SearchSelectMulti';
import { CompactMenuItem } from 'tg.views/projects/translations/Filters/FiltersComponents';
import { StyledInputContent } from 'tg.component/searchSelect/SearchStyled';
import { CircledLanguageIcon } from 'tg.component/languages/CircledLanguageIcon';
import { LanguageModel } from 'tg.component/PermissionsSettings/types';
import { isAllLanguages } from 'tg.ee/PermissionsAdvanced/hierarchyTools';

const StyledButton = styled(Button)`
  padding: 0px;
  padding-left: 7px;
  padding-right: 5px;
`;

export const LanguagePermissionsMenu: FunctionComponent<{
  selected: number[];
  onSelect: (value: number[]) => void;
  buttonProps?: ComponentProps<typeof Button>;
  disabled?: boolean | number[];
  allLanguages: LanguageModel[];
  selectAllButton?: boolean;
}> = (props) => {
  const anchorEl = useRef<HTMLButtonElement>(null);
  const [open, setOpen] = useState(false);
  const { t } = useTranslate();

  const handleClose = () => {
    setOpen(false);
  };

  const handleClick = () => {
    setOpen(true);
  };

  const disabledLanguages = Array.isArray(props.disabled)
    ? (props.disabled as number[])
    : [];

  const selected =
    disabledLanguages.length && isAllLanguages(props.selected)
      ? props.allLanguages.map((l) => l.id)
      : props.selected;

  const selectedLanguages = selected
    .map((id) => props.allLanguages.find((l) => l.id === id)!)
    .filter(Boolean);

  const handleSelectAll = () => {
    props.onSelect([]);
    handleClose();
  };

  const handleToggle = (langId: number) => {
    if (selected.includes(langId)) {
      props.onSelect(selected.filter((id) => id !== langId));
    } else {
      props.onSelect([...props.selected, langId]);
    }
  };

  const selectedIds = selectedLanguages.map((l) => l.id);

  const allItems = props.allLanguages.map((language) => ({
    value: language.id,
    name: language.name,
  }));

  const isAll = isAllLanguages(props.selected);

  const listIsLong = props.allLanguages.length > 10;

  return (
    <>
      <Tooltip
        title={
          props.selected?.length
            ? selectedLanguages.map((l) => l.name || l.tag).join(', ')
            : t('languages_permitted_list_all')
        }
        disableInteractive
      >
        <StyledButton
          data-cy="permissions-language-menu-button"
          disabled={props.disabled === true}
          ref={anchorEl}
          {...props.buttonProps}
          size="small"
          variant="outlined"
          aria-controls="simple-menu"
          aria-haspopup="true"
          onClick={handleClick}
        >
          <LanguagesPermittedList
            languages={props.selected.map(
              (lid) => props.allLanguages.find((l) => l.id === lid)!
            )}
            disabled={props.disabled}
            maxItems={5}
          />
          <ArrowDropDown fontSize="small" />
        </StyledButton>
      </Tooltip>
      {anchorEl && (
        <Popover
          anchorEl={anchorEl.current}
          open={open}
          onClose={handleClose}
          disablePortal={false}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'center',
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'center',
          }}
        >
          <SearchSelectMulti
            displaySearch={listIsLong}
            searchPlaceholder={t('language_permitted_search')}
            open={Boolean(anchorEl)}
            items={allItems}
            value={selectedIds}
            anchorEl={anchorEl.current || undefined}
            onAction={!isAll && listIsLong ? handleSelectAll : undefined}
            actionIcon={<CheckBox fontSize="small" />}
            actionTooltip={t('languages_permitted_list_select_all')}
            onSelect={handleToggle}
            maxWidth={400}
            renderOption={(renderProps, option) => (
              <CompactMenuItem
                key={option.value}
                {...renderProps}
                data-cy="search-select-item"
                disabled={disabledLanguages.includes(option.value)}
              >
                <Checkbox
                  size="small"
                  edge="start"
                  checked={selectedIds.includes(option.value)}
                />
                <StyledInputContent>{option.name}</StyledInputContent>
                <CircledLanguageIcon
                  size={20}
                  flag={
                    props.allLanguages.find(({ id }) => id === option.value)
                      ?.flagEmoji
                  }
                />
              </CompactMenuItem>
            )}
          />
        </Popover>
      )}
    </>
  );
};
