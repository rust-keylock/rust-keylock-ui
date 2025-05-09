use j4rs::errors::J4RsError;
// Copyright 2017 astonbitecode
// This file is part of rust-keylock password manager.
//
// rust-keylock is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// rust-keylock is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with rust-keylock.  If not, see <http://www.gnu.org/licenses/>.
use log4rs;
use std::sync::TryLockError;
use std::{fmt, result, io};
use std::error::Error;
use log;
use anyhow;

pub type Result<T> = result::Result<T, RklUiError>;

#[derive(Debug)]
pub struct RklUiError {
    description: String
}

// TODO: Remove if not needed
// impl RklUiError {
//     pub fn new(message: &str) -> RklUiError {
//         RklUiError { description: format!("{:?}", message) }
//     }
// }

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

impl From<log4rs::config::runtime::ConfigError> for RklUiError {
    fn from(err: log4rs::config::runtime::ConfigError) -> RklUiError {
        RklUiError { description: format!("{:?}", err) }
    }
}

impl From<log4rs::config::runtime::ConfigErrors> for RklUiError {
    fn from(err: log4rs::config::runtime::ConfigErrors) -> RklUiError {
        RklUiError { description: format!("{:?}", err) }
    }
}

impl From<anyhow::Error> for RklUiError {
    fn from(err: anyhow::Error) -> RklUiError {
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

impl From<Box<dyn Error + Send + Sync>> for RklUiError {
    fn from(err: Box<dyn Error + Send + Sync>) -> RklUiError {
        RklUiError { description: format!("{:?}", err) }
    }
}

impl From<J4RsError> for RklUiError {
    fn from(err: J4RsError) -> RklUiError {
        RklUiError { description: format!("{:?}", err) }
    }
}

impl <T> From<TryLockError<T>> for RklUiError {
    fn from(err: TryLockError<T>) -> RklUiError {
        RklUiError { description: format!("{:?}", err) }
    }
}
