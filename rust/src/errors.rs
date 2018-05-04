use log4rs;
use std::{fmt, result, io};
use std::error::Error;
use log;

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

impl From<log4rs::config::Errors> for RklUiError {
    fn from(err: log4rs::config::Errors) -> RklUiError {
        RklUiError { description: format!("{:?}", err) }
    }
}

impl From<log::SetLoggerError> for RklUiError {
    fn from(err: log::SetLoggerError) -> RklUiError {
        RklUiError { description: format!("{:?}", err) }
    }
}

impl From<io::Error> for RklUiError {
    fn from(err: io::Error) -> RklUiError {
        RklUiError { description: format!("{:?}", err) }
    }
}
