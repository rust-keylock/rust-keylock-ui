use std::error::Error;
use std::{fmt, result};

pub type Result<T> = result::Result<T, RklUiError>;

#[derive(Debug)]
pub struct RklUiError {
    description: String
}

impl fmt::Display for RklUiError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.description)
    }
}

impl Error for RklUiError {
    fn description(&self) -> &str {
        self.description.as_str()
    }
}