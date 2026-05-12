import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Slider, Grid, Button,
  CircularProgress, Divider,
  TextField, Switch, FormControlLabel,
  MenuItem, Select, FormControl, InputLabel
} from '@mui/material';
import { updatePreferences } from '../../api/userApi';
import toast from 'react-hot-toast';

const marks = [
  { value: 1, label: '1' },
  { value: 2, label: '2' },
  { value: 3, label: '3' },
  { value: 4, label: '4' },
  { value: 5, label: '5' }
];

const SliderField = ({ label, name, value,
  onChange, minLabel, maxLabel }) => (
  <Box sx={{ mb: 3 }}>
    <Typography gutterBottom fontWeight="medium">
      {label}
    </Typography>
    <Box sx={{ px: 1 }}>
      <Slider
        value={value || 3}
        min={1} max={5} step={1}
        marks={marks}
        onChange={(e, val) => onChange(name, val)}
        valueLabelDisplay="auto"
        sx={{ color: '#667eea' }}
      />
      <Box sx={{
        display: 'flex',
        justifyContent: 'space-between'
      }}>
        <Typography variant="caption" color="text.secondary">
          {minLabel}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {maxLabel}
        </Typography>
      </Box>
    </Box>
  </Box>
);

const PreferencesForm = ({ existingPreferences, onSaved }) => {
  const [prefs, setPrefs] = useState({
    minBudget: '',
    maxBudget: '',
    city: '',
    moveInDate: '',
    petsAllowed: false,
    smokingAllowed: false,
    genderPreference: 'any',
    sleepSchedule: 3,
    cleanlinessLevel: 3,
    socialHabits: 3,
    workFromHome: 3,
    guestFrequency: 3,
    noiseLevel: 3
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (existingPreferences) {
      setPrefs(prev => ({
        ...prev,
        ...existingPreferences,
        moveInDate: existingPreferences.moveInDate || '',
        genderPreference: existingPreferences.genderPreference
          || 'any'
      }));
    }
  }, [existingPreferences]);

  const handleChange = (name, value) => {
    setPrefs(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async () => {
    if (!prefs.city) {
      toast.error('Please enter your preferred city');
      return;
    }
    if (!prefs.minBudget || !prefs.maxBudget) {
      toast.error('Please enter your budget range');
      return;
    }
    setLoading(true);
    try {
      await updatePreferences(prefs);
      toast.success('Preferences saved!');
      if (onSaved) onSaved();
    } catch (err) {
      toast.error('Failed to save preferences');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h6" fontWeight="bold" sx={{ mb: 2 }}>
        Basic Info
      </Typography>
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Preferred City"
            value={prefs.city || ''}
            onChange={(e) => handleChange('city', e.target.value)}
          />
        </Grid>
        <Grid item xs={6}>
          <TextField
            fullWidth
            type="number"
            label="Min Budget"
            value={prefs.minBudget || ''}
            onChange={(e) => handleChange('minBudget', e.target.value)}
          />
        </Grid>
        <Grid item xs={6}>
          <TextField
            fullWidth
            type="number"
            label="Max Budget"
            value={prefs.maxBudget || ''}
            onChange={(e) => handleChange('maxBudget', e.target.value)}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            type="date"
            label="Move-in Date"
            InputLabelProps={{ shrink: true }}
            value={prefs.moveInDate || ''}
            onChange={(e) => handleChange('moveInDate', e.target.value)}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <FormControl fullWidth>
            <InputLabel>Gender Preference</InputLabel>
            <Select
              label="Gender Preference"
              value={prefs.genderPreference || 'any'}
              onChange={(e) => handleChange('genderPreference', e.target.value)}
            >
              <MenuItem value="any">Any</MenuItem>
              <MenuItem value="male">Male</MenuItem>
              <MenuItem value="female">Female</MenuItem>
            </Select>
          </FormControl>
        </Grid>
      </Grid>

      <Divider sx={{ mb: 3 }} />

      <Typography variant="h6" fontWeight="bold" sx={{ mb: 2 }}>
        House Rules
      </Typography>
      <Box sx={{ display: 'flex', gap: 4, mb: 3 }}>
        <FormControlLabel
          control={
            <Switch
              checked={!!prefs.petsAllowed}
              onChange={(e) => handleChange('petsAllowed', e.target.checked)}
            />
          }
          label="Pets Allowed"
        />
        <FormControlLabel
          control={
            <Switch
              checked={!!prefs.smokingAllowed}
              onChange={(e) => handleChange('smokingAllowed', e.target.checked)}
            />
          }
          label="Smoking Allowed"
        />
      </Box>

      <Divider sx={{ mb: 3 }} />

      <Typography variant="h6" fontWeight="bold" sx={{ mb: 2 }}>
        Lifestyle
      </Typography>
      <SliderField
        label="Sleep Schedule"
        name="sleepSchedule"
        value={prefs.sleepSchedule}
        onChange={handleChange}
        minLabel="Early bird"
        maxLabel="Night owl"
      />
      <SliderField
        label="Cleanliness Level"
        name="cleanlinessLevel"
        value={prefs.cleanlinessLevel}
        onChange={handleChange}
        minLabel="Relaxed"
        maxLabel="Very clean"
      />
      <SliderField
        label="Social Habits"
        name="socialHabits"
        value={prefs.socialHabits}
        onChange={handleChange}
        minLabel="Very introverted"
        maxLabel="Very extroverted"
      />
      <SliderField
        label="Work From Home"
        name="workFromHome"
        value={prefs.workFromHome}
        onChange={handleChange}
        minLabel="Never"
        maxLabel="Always"
      />
      <SliderField
        label="Guest Frequency"
        name="guestFrequency"
        value={prefs.guestFrequency}
        onChange={handleChange}
        minLabel="Never"
        maxLabel="Very often"
      />
      <SliderField
        label="Noise Level"
        name="noiseLevel"
        value={prefs.noiseLevel}
        onChange={handleChange}
        minLabel="Very quiet"
        maxLabel="Lively"
      />

      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
        <Button
          variant="contained"
          onClick={handleSubmit}
          disabled={loading}
          sx={{
            bgcolor: '#667eea',
            '&:hover': { bgcolor: '#5568d3' },
            borderRadius: 2,
            px: 4
          }}
        >
          {loading ? <CircularProgress size={24} color="inherit" /> : 'Save Preferences'}
        </Button>
      </Box>
    </Box>
  );
};

export default PreferencesForm;
