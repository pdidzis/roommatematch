import React from 'react';
import {
  Box, TextField, MenuItem, Select,
  FormControl, InputLabel, Button,
  InputAdornment, Slider, Typography
} from '@mui/material';
import { Search, FilterList, Clear } from '@mui/icons-material';

const ListingFilters = ({ filters, onChange, onClear }) => {
  const hasActiveFilter =
    filters.city ||
    filters.maxRent ||
    filters.petsAllowed !== '' ||
    filters.smokingAllowed !== '';

  return (
    <Box
      sx={{
        display: 'flex',
        gap: 2,
        flexWrap: 'wrap',
        alignItems: 'center',
        mb: 3,
        p: 2,
        bgcolor: 'white',
        borderRadius: 2,
        boxShadow: '0 2px 8px rgba(0,0,0,0.06)'
      }}
    >
      <TextField
        placeholder="Search by city..."
        value={filters.city}
        onChange={(e) => onChange('city', e.target.value)}
        size="small"
        sx={{ width: 200 }}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <Search fontSize="small" />
            </InputAdornment>
          )
        }}
      />

      <TextField
        type="number"
        label="Max Rent (€)"
        value={filters.maxRent}
        onChange={(e) => onChange('maxRent', e.target.value)}
        size="small"
        sx={{ width: 150 }}
      />

      <FormControl size="small" sx={{ width: 130 }}>
        <InputLabel>Pets</InputLabel>
        <Select
          label="Pets"
          value={filters.petsAllowed}
          onChange={(e) => onChange('petsAllowed', e.target.value)}
        >
          <MenuItem value="">Any</MenuItem>
          <MenuItem value="true">Allowed</MenuItem>
          <MenuItem value="false">Not allowed</MenuItem>
        </Select>
      </FormControl>

      <FormControl size="small" sx={{ width: 150 }}>
        <InputLabel>Smoking</InputLabel>
        <Select
          label="Smoking"
          value={filters.smokingAllowed}
          onChange={(e) => onChange('smokingAllowed', e.target.value)}
        >
          <MenuItem value="">Any</MenuItem>
          <MenuItem value="true">Allowed</MenuItem>
          <MenuItem value="false">Not allowed</MenuItem>
        </Select>
      </FormControl>

      {hasActiveFilter && (
        <Button
          startIcon={<Clear />}
          onClick={onClear}
          size="small"
        >
          Clear
        </Button>
      )}
    </Box>
  );
};

export default ListingFilters;
